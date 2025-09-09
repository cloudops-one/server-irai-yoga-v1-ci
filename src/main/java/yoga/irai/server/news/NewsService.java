package yoga.irai.server.news;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.NewsMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

@Service
@AllArgsConstructor
public class NewsService {
    private final UserService userService;
    private final StorageService storageService;
    private final SettingService settingService;
    private final NewsRepository newsRepository;
    private final NotificationService notificationService;
    private final OrganizationService organizationService;

    /**
     * Adds a new news entity to the repository.
     *
     * @param newsRequestDto
     *            the DTO containing news details
     * @return the saved NewsEntity
     */
    public NewsEntity addNews(@Valid NewsRequestDto newsRequestDto) {
        if (newsRepository.existsByNewsName(newsRequestDto.getNewsName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        NewsEntity newsEntity = AppUtils.map(newsRequestDto, NewsEntity.class);
        if (ObjectUtils.isEmpty(newsEntity.getNewsIconStorageId())) {
            newsEntity.setNewsIconStorageId(null);
        }
        if (ObjectUtils.isEmpty(newsEntity.getNewsBannerStorageId())) {
            newsEntity.setNewsBannerStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(newsRequestDto.getTags())) {
            newsEntity.setTags(AppUtils.writeValueAsString(newsRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.NEWS_TAGS, newsRequestDto.getTags());
        }
        return newsRepository.save(newsEntity);
    }

    /**
     * Updates an existing news entity.
     *
     * @param newsId
     *            the ID of the news entity to update
     * @param newsRequestDto
     *            the DTO containing updated news details
     * @return the updated NewsEntity
     */
    @Transactional
    public NewsEntity updateNews(UUID newsId, NewsRequestDto newsRequestDto) {
        NewsEntity newsEntity = getNewsById(newsId);
        if (ObjectUtils.isEmpty(newsEntity.getNewsIconExternalUrl())
                && ObjectUtils.isNotEmpty(newsRequestDto.getNewsIconStorageId())
                && !newsRequestDto.getNewsIconStorageId().equals(newsEntity.getNewsIconStorageId())) {
            storageService.deleteStorageById(newsEntity.getNewsIconStorageId());
        }
        if (ObjectUtils.isEmpty(newsEntity.getNewsBannerExternalUrl())
                && ObjectUtils.isNotEmpty(newsRequestDto.getNewsBannerStorageId())
                && !newsRequestDto.getNewsBannerStorageId().equals(newsEntity.getNewsBannerStorageId())) {
            storageService.deleteStorageById(newsEntity.getNewsBannerStorageId());
        }
        AppUtils.map(newsRequestDto, newsEntity);
        newsEntity.setNewsId(newsId);
        if (ObjectUtils.isNotEmpty(newsRequestDto.getNewsIconStorageId())) {
            newsEntity.setNewsIconExternalUrl(null);
            newsEntity.setNewsIconStorageId(newsRequestDto.getNewsIconStorageId());
        } else {
            newsEntity.setNewsIconExternalUrl(newsEntity.getNewsIconExternalUrl());
            newsEntity.setNewsIconStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(newsRequestDto.getNewsBannerStorageId())) {
            newsEntity.setNewsBannerStorageId(newsRequestDto.getNewsBannerStorageId());
            newsEntity.setNewsBannerExternalUrl(null);
        } else {
            newsEntity.setNewsBannerExternalUrl(newsRequestDto.getNewsBannerExternalUrl());
            newsEntity.setNewsBannerStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(newsRequestDto.getTags())) {
            newsEntity.setTags(AppUtils.writeValueAsString(newsRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.NEWS_TAGS, newsRequestDto.getTags());
        }
        return newsRepository.save(newsEntity);
    }

    /**
     * Retrieves a news entity by its ID.
     *
     * @param newsId
     *            the ID of the news entity
     * @return the NewsEntity if found
     */
    public NewsEntity getNewsById(UUID newsId) {
        return newsRepository.findById(newsId).orElseThrow(AppUtils.Messages.NEWS_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of news entities based on the provided parameters.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the direction of sorting (ASC or DESC)
     * @param keyword
     *            a keyword to filter news by name or description
     * @return a Page containing NewsEntity objects
     */
    public Page<NewsEntity> getNews(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        return newsRepository.search(keyword, pageable);
    }

    /**
     * Deletes a news entity by its ID.
     *
     * @param newsId
     *            the list of news IDs
     */
    public void deleteNews(UUID newsId) {
        NewsEntity newsEntity = getNewsById(newsId);
        Set<UUID> storageIds = Stream.of(newsEntity.getNewsIconStorageId(), newsEntity.getNewsBannerStorageId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        newsRepository.deleteById(newsId);
    }

    /**
     * Changes the status of a news entity.
     *
     * @param newsId
     *            the ID of the news entity
     * @param status
     *            the new status to set
     */
    public void changeNewsStatus(UUID newsId, AppUtils.NewsStatus status) {
        NewsEntity newsEntity = getNewsById(newsId);
        newsEntity.setNewsStatus(status);
        newsRepository.save(newsEntity);
        if (AppUtils.NewsStatus.ACTIVE.equals(status)) {
            sendNewsNotification(toNewsResponseDto(newsEntity));
        }
    }

    /**
     * Sends a notification for the given news.
     *
     * @param newsResponseDto
     *            the DTO containing news details
     */
    private void sendNewsNotification(NewsResponseDto newsResponseDto) {
        try {
            String title = newsResponseDto.getNewsName();
            String body = newsResponseDto.getNewsDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(newsResponseDto.getNewsBannerStorageUrl())) {
                imageUrl = newsResponseDto.getNewsBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(newsResponseDto.getNewsBannerExternalUrl())) {
                imageUrl = newsResponseDto.getNewsBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.NEWS, title, body, imageUrl,
                    newsResponseDto.getNewsId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * Converts a list of NewsEntity objects to a list of NewsResponseDto objects.
     *
     * @param newsEntities
     *            the list of NewsEntity objects
     * @return a list of NewsResponseDto objects
     */
    public List<NewsResponseDto> toNewsResponseDtos(List<NewsEntity> newsEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(newsEntities);
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(newsEntities.stream().flatMap(
                shortsEntity -> Stream.of(shortsEntity.getNewsIconStorageId(), shortsEntity.getNewsBannerStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        return newsEntities.stream().map(newsEntity -> {
            NewsResponseDto newsResponseDto = AppUtils.map(newsEntity, NewsResponseDto.class);
            newsResponseDto.setCreatedByName(userNamesByIds.get(newsEntity.getCreatedBy()));
            newsResponseDto.setUpdatedByName(userNamesByIds.get(newsEntity.getUpdatedBy()));
            newsResponseDto.setNewsIconStorageUrl(signedStorageUrlByIds.get(newsEntity.getNewsIconStorageId()));
            newsResponseDto.setNewsBannerStorageUrl(signedStorageUrlByIds.get(newsEntity.getNewsBannerStorageId()));
            newsResponseDto.setTags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
            }));
            return newsResponseDto;
        }).toList();
    }

    /**
     * Converts a list of NewsEntity objects to a list of NewsMobileResponseDto
     * objects.
     *
     * @param newsEntities
     *            the list of NewsEntity objects
     * @return a list of NewsMobileResponseDto objects
     */
    public List<NewsMobileResponseDto> toNewsMobileResponseDto(List<NewsEntity> newsEntities) {
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(newsEntities.stream().flatMap(
                shortsEntity -> Stream.of(shortsEntity.getNewsIconStorageId(), shortsEntity.getNewsBannerStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        return newsEntities.stream().map(newsEntity -> {
            NewsMobileResponseDto newsMobileResponseDto = AppUtils.map(newsEntity, NewsMobileResponseDto.class);
            newsMobileResponseDto.setNewsIconStorageUrl(signedStorageUrlByIds.get(newsEntity.getNewsIconStorageId()));
            newsMobileResponseDto
                    .setNewsBannerStorageUrl(signedStorageUrlByIds.get(newsEntity.getNewsBannerStorageId()));
            newsMobileResponseDto.setTags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
            }));
            return newsMobileResponseDto;
        }).toList();
    }

    /**
     * Retrieves the top news entities for the portal dashboard.
     *
     * @return a list of top NewsEntity objects
     */
    public List<NewsEntity> getTopNews() {
        return newsRepository.getNewsEntitiesByNewsStatusOrderByCreatedAtDesc(AppUtils.NewsStatus.ACTIVE);
    }

    /**
     * Retrieves the top news entities for the portal dashboard.
     *
     * @return a list of top NewsEntity objects
     */
    public List<NewsEntity> getTop3News() {
        return newsRepository.findTop3ByNewsStatusOrderByCreatedAtDesc(AppUtils.NewsStatus.ACTIVE);
    }

    /**
     * Increments the view count of a news entity by 1.
     *
     * @param newsId
     *            the ID of the news entity
     */
    public void updateViewCount(UUID newsId) {
        NewsEntity newsEntity = getNewsById(newsId);
        newsEntity.setViews(newsEntity.getViews() + 1);
        newsRepository.save(newsEntity);
    }

    /**
     * Increments the like count of a news entity by 1.
     *
     * @param newsId
     *            the ID of the news entity
     */
    public void updateLikeCount(UUID newsId) {
        NewsEntity newsEntity = getNewsById(newsId);
        newsEntity.setLikes(newsEntity.getLikes() + 1);
        newsRepository.save(newsEntity);
    }

    /**
     * Converts a NewsEntity to a NewsResponseDto.
     *
     * @param newsEntity
     *            the NewsEntity to convert
     * @return the converted NewsResponseDto
     */
    public NewsResponseDto toNewsResponseDto(NewsEntity newsEntity) {
        NewsResponseDto newsResponseDto = AppUtils.map(newsEntity, NewsResponseDto.class);
        newsResponseDto.setNewsBannerStorageUrl(storageService.getStorageUrl(newsEntity.getNewsBannerStorageId()));
        newsResponseDto.setNewsIconStorageUrl(storageService.getStorageUrl(newsEntity.getNewsIconStorageId()));
        newsResponseDto.setCreatedByName(userService.getUserNameById(newsEntity.getCreatedBy()));
        newsResponseDto.setUpdatedByName(userService.getUserNameById(newsEntity.getUpdatedBy()));
        newsResponseDto.setTags(AppUtils.readValue(newsEntity.getTags(), new TypeReference<>() {
        }));
        return newsResponseDto;
    }
}
