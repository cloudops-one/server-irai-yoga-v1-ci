package yoga.irai.server.shorts;

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
import yoga.irai.server.mobile.dto.ShortsMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

/**
 * This service handles operations related to shorts in the application.
 */
@Service
@AllArgsConstructor
public class ShortsService {

    private final UserService userService;
    private final StorageService storageService;
    private final SettingService settingService;
    private final ShortsRepository shortsRepository;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;

    /**
     * Adds anew shorts
     *
     * @param shortsRequestDto
     *            the shorts data transfer object containing shorts details
     *
     * @return the newly added shorts
     */
    public ShortsEntity addShorts(@Valid ShortsRequestDto shortsRequestDto) {
        if (shortsRepository.existsByShortsName(shortsRequestDto.getShortsName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        ShortsEntity shortsEntity = AppUtils.map(shortsRequestDto, ShortsEntity.class);
        if (ObjectUtils.isEmpty(shortsEntity.getShortsStorageId())) {
            shortsEntity.setShortsStorageId(null);
        }
        if (ObjectUtils.isEmpty(shortsEntity.getShortsBannerStorageId())) {
            shortsEntity.setShortsBannerStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(shortsRequestDto.getTags())) {
            shortsEntity.setTags(AppUtils.writeValueAsString(shortsRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.SHORTS_TAGS, shortsRequestDto.getTags());
        }
        return shortsRepository.save(shortsEntity);
    }

    /**
     * Updates an existing shorts.
     *
     * @param shortsId
     *            the ID of the shorts to update
     * @param shortsRequestDto
     *            the updated shorts data transfer object
     * @return the updated shorts audit
     */
    @Transactional
    public ShortsEntity updateShorts(UUID shortsId, ShortsRequestDto shortsRequestDto) {
        ShortsEntity shortsEntity = getShortsById(shortsId);
        if (ObjectUtils.isEmpty(shortsEntity.getShortsBannerExternalUrl())
                && ObjectUtils.isNotEmpty(shortsRequestDto.getShortsBannerStorageId())
                && !shortsRequestDto.getShortsBannerStorageId().equals(shortsEntity.getShortsBannerStorageId())) {
            storageService.deleteStorageById(shortsEntity.getShortsBannerStorageId());
        }
        if (ObjectUtils.isEmpty(shortsEntity.getShortsExternalUrl())
                && ObjectUtils.isNotEmpty(shortsRequestDto.getShortsStorageId())
                && !shortsRequestDto.getShortsStorageId().equals(shortsEntity.getShortsStorageId())) {
            storageService.deleteStorageById(shortsEntity.getShortsStorageId());
        }
        AppUtils.map(shortsRequestDto, shortsEntity);
        shortsEntity.setShortsId(shortsId);
        if (ObjectUtils.isNotEmpty(shortsRequestDto.getShortsStorageId())) {
            shortsEntity.setShortsExternalUrl(null);
            shortsEntity.setShortsStorageId(shortsRequestDto.getShortsStorageId());
        } else {
            shortsEntity.setShortsExternalUrl(shortsEntity.getShortsExternalUrl());
            shortsEntity.setShortsStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(shortsRequestDto.getShortsBannerStorageId())) {
            shortsEntity.setShortsBannerStorageId(shortsRequestDto.getShortsBannerStorageId());
            shortsEntity.setShortsBannerExternalUrl(null);
        } else {
            shortsEntity.setShortsBannerExternalUrl(shortsRequestDto.getShortsBannerExternalUrl());
            shortsEntity.setShortsBannerStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(shortsRequestDto.getTags())) {
            shortsEntity.setTags(AppUtils.writeValueAsString(shortsRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.SHORTS_TAGS, shortsRequestDto.getTags());
        }
        return shortsRepository.save(shortsEntity);
    }

    /**
     * Retrieves a shorts by its ID.
     *
     * @param shortsId
     *            the ID of the shorts to retrieve
     * @return the shorts audit if found, otherwise throws an exception
     */
    public ShortsEntity getShortsById(UUID shortsId) {
        return shortsRepository.findById(shortsId).orElseThrow(AppUtils.Messages.SHORTS_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of shorts.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the direction of sorting (ascending or descending)
     * @param keyword
     *            an optional keyword to filter shorts by name or description
     * @return a paginated list of shorts entities
     */
    public Page<ShortsEntity> getShorts(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());

        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> shortsRepository.search(keyword, organizationService.getOrgIdsForMobile(),
                    AppUtils.ShortsStatus.ACTIVE, pageable);
            case PORTAL_USER -> shortsRepository.search(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> shortsRepository.search(keyword, null, null, pageable);
        };
    }

    /**
     * Deletes a shorts by its ID.
     *
     * @param shortsId
     *            the ID of the shorts to delete
     */
    public void deleteShorts(UUID shortsId) {
        ShortsEntity shortsEntity = getShortsById(shortsId);
        Set<UUID> storageIds = Stream.of(shortsEntity.getShortsStorageId(), shortsEntity.getShortsBannerStorageId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        shortsRepository.deleteById(shortsId);
    }

    /**
     * Changes the status of a shorts.
     *
     * @param shortsId
     *            the ID of the shorts to update
     * @param status
     *            the new status to set for the shorts
     */
    public void changeShortsStatus(UUID shortsId, AppUtils.ShortsStatus status) {
        ShortsEntity shortsEntity = getShortsById(shortsId);
        shortsEntity.setShortsStatus(status);
        shortsRepository.save(shortsEntity);
        if (status == AppUtils.ShortsStatus.ACTIVE) {
            sendShortsNotification(getShortsResponseDto(shortsEntity));
        }
    }

    /**
     * Updates the view count of a shorts.
     *
     * @param shortsId
     *            the ID of the shorts to update
     */
    public void updateViewCount(UUID shortsId) {
        ShortsEntity shortsEntity = getShortsById(shortsId);
        shortsEntity.setViews(shortsEntity.getViews() + 1);
        shortsRepository.save(shortsEntity);
    }

    /**
     * Updates the like count of a shorts.
     *
     * @param shortsId
     *            the ID of the shorts to update
     */
    public void updateLikeCount(UUID shortsId) {
        ShortsEntity shortsEntity = getShortsById(shortsId);
        shortsEntity.setLikes(shortsEntity.getLikes() + 1);
        shortsRepository.save(shortsEntity);
    }

    /**
     * Retrieves the total number of shorts.
     *
     * @return the total count of shorts
     */
    public Long getTotalShorts() {
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER -> shortsRepository.count();
            case PORTAL_USER -> shortsRepository.countByOrgId(AppUtils.getPrincipalOrgId());
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Retrieves the top 3 shorts for the current organization.
     *
     * @return a list of the top 3 shorts entities
     */
    public List<ShortsEntity> getTop3Shorts() {
        return shortsRepository.getTop3ByOrgIdAndShortsStatusOrderByCreatedAtDesc(AppUtils.getPrincipalOrgId(),
                AppUtils.ShortsStatus.ACTIVE);
    }

    /**
     * Sends a notification for the given shorts.
     *
     * @param shortsResponseDto
     *            the DTO containing shorts details
     */
    private void sendShortsNotification(ShortsResponseDto shortsResponseDto) {
        try {
            String title = shortsResponseDto.getShortsName();
            String body = shortsResponseDto.getShortsDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(shortsResponseDto.getShortsBannerStorageUrl())) {
                imageUrl = shortsResponseDto.getShortsBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(shortsResponseDto.getShortsBannerExternalUrl())) {
                imageUrl = shortsResponseDto.getShortsBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.SHORTS, title, body, imageUrl,
                    shortsResponseDto.getShortsId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * Converts a ShortsEntity to a ShortsResponseDto, enriching it with additional
     * data.
     *
     * @param shortsEntity
     *            the ShortsEntity to convert
     * @return the enriched ShortsResponseDto
     */
    public ShortsResponseDto getShortsResponseDto(ShortsEntity shortsEntity) {
        ShortsResponseDto shortsResponseDto = AppUtils.map(shortsEntity, ShortsResponseDto.class);
        shortsResponseDto.setShortsStorageUrl(storageService.getStorageUrl(shortsEntity.getShortsStorageId()));
        shortsResponseDto
                .setShortsBannerStorageUrl(storageService.getStorageUrl(shortsEntity.getShortsBannerStorageId()));
        shortsResponseDto.setOrgName(organizationService.getOrgNameByOrgId(shortsEntity.getOrgId()));
        shortsResponseDto
                .setOrgIconStorageUrl(organizationService.getOrgIconStorageIdToSignedIconUrl(shortsEntity.getOrgId()));
        shortsResponseDto.setCreatedByName(userService.getUserNameById(shortsEntity.getCreatedBy()));
        shortsResponseDto.setUpdatedByName(userService.getUserNameById(shortsEntity.getUpdatedBy()));
        shortsResponseDto.setTags(AppUtils.readValue(shortsEntity.getTags(), new TypeReference<>() {
        }));
        return shortsResponseDto;
    }

    /**
     * Converts a list of ShortsEntity to a list of ShortsResponseDto.
     *
     * @param shortsEntities
     *            the list of ShortsEntity to convert
     * @return a list of ShortsResponseDto
     */
    public List<ShortsResponseDto> toShortsResponseDto(List<ShortsEntity> shortsEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(shortsEntities);
        Map<UUID, String> signedStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(
                        shortsEntities.stream()
                                .flatMap(shortsEntity -> Stream.of(shortsEntity.getShortsStorageId(),
                                        shortsEntity.getShortsBannerStorageId()))
                                .filter(Objects::nonNull).distinct().toList());
        return shortsEntities.stream().map(shortsEntity -> {
            ShortsResponseDto shortsResponseDto = AppUtils.map(shortsEntity, ShortsResponseDto.class);
            shortsResponseDto.setCreatedByName(userNamesByIds.get(shortsEntity.getCreatedBy()));
            shortsResponseDto.setUpdatedByName(userNamesByIds.get(shortsEntity.getUpdatedBy()));
            shortsResponseDto.setShortsStorageUrl(signedStorageUrlByIds.get(shortsEntity.getShortsStorageId()));
            shortsResponseDto
                    .setShortsBannerStorageUrl(signedStorageUrlByIds.get(shortsEntity.getShortsBannerStorageId()));
            shortsResponseDto.setTags(AppUtils.readValue(shortsEntity.getTags(), new TypeReference<>() {
            }));
            return shortsResponseDto;
        }).toList();
    }

    /**
     * Converts a list of ShortsEntity to a list of ShortsMobileResponseDto.
     *
     * @param shortsEntities
     *            the list of ShortsEntity to convert
     * @return a list of ShortsMobileResponseDto
     */
    public List<ShortsMobileResponseDto> toShortsMobileResponseDto(List<ShortsEntity> shortsEntities) {

        Map<UUID, String> signedStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(
                        shortsEntities.stream()
                                .flatMap(shortsEntity -> Stream.of(shortsEntity.getShortsStorageId(),
                                        shortsEntity.getShortsBannerStorageId()))
                                .filter(Objects::nonNull).distinct().toList());
        return shortsEntities.stream().map(shortsEntity -> {
            ShortsMobileResponseDto shortsMobileResponseDto = AppUtils.map(shortsEntity, ShortsMobileResponseDto.class);
            shortsMobileResponseDto.setShortsStorageUrl(signedStorageUrlByIds.get(shortsEntity.getShortsStorageId()));
            shortsMobileResponseDto
                    .setShortsBannerStorageUrl(signedStorageUrlByIds.get(shortsEntity.getShortsBannerStorageId()));
            shortsMobileResponseDto.setTags(AppUtils.readValue(shortsEntity.getTags(), new TypeReference<>() {
            }));
            return shortsMobileResponseDto;
        }).toList();
    }
}
