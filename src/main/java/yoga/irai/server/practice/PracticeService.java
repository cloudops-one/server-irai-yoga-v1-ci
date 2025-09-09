package yoga.irai.server.practice;

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
import yoga.irai.server.mobile.dto.PracticeMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.practice.category.PracticeCategoryService;
import yoga.irai.server.practice.user.PracticeUserEntity;
import yoga.irai.server.practice.user.PracticeUserRatingUpdateDto;
import yoga.irai.server.practice.user.PracticeUserRepository;
import yoga.irai.server.practice.user.PracticeUserRequestDto;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

/**
 * Service class for managing practices. Provides methods to add, update,
 * retrieve, and delete practices.
 */
@Service
@AllArgsConstructor
public class PracticeService {
    private final UserService userService;
    private final StorageService storageService;
    private final SettingService settingService;
    private final PracticeRepository practiceRepository;
    private final OrganizationService organizationService;
    private final PracticeUserRepository practiceUserRepository;
    private final PracticeCategoryService practiceCategoryService;
    private final NotificationService notificationService;

    /**
     * Adds a new practice.
     *
     * @param practiceRequestDto
     *            the request DTO containing practice details
     * @return the saved PracticeEntity
     */
    public PracticeEntity addPractice(@Valid PracticeRequestDto practiceRequestDto) {
        if (practiceRepository.existsByPracticeName(practiceRequestDto.getPracticeName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        PracticeEntity practiceEntity = AppUtils.map(practiceRequestDto, PracticeEntity.class);
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeStorageId())) {
            practiceEntity.setPracticeStorageId(null);
        }
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeBannerStorageId())) {
            practiceEntity.setPracticeBannerStorageId(null);
        }
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeIconStorageId())) {
            practiceEntity.setPracticeIconStorageId(null);
        }
        practiceEntity.setPracticeId(null);
        if (ObjectUtils.isNotEmpty(practiceEntity.getTags())) {
            practiceEntity.setTags(AppUtils.writeValueAsString(practiceRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.PRACTICE_TAGS, practiceRequestDto.getTags());
        }
        return practiceRepository.save(practiceEntity);
    }

    /**
     * Updates an existing practice.
     *
     * @param practiceId
     *            the ID of the practice to update
     * @param practiceRequestDto
     *            the request DTO containing updated practice details
     * @return the updated PracticeEntity
     */
    @Transactional
    public PracticeEntity updatePractice(UUID practiceId, @Valid PracticeRequestDto practiceRequestDto) {
        PracticeEntity practiceEntity = getPracticeById(practiceId);
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeIconExternalUrl())
                && ObjectUtils.isNotEmpty(practiceRequestDto.getPracticeIconStorageId())
                && !practiceRequestDto.getPracticeIconStorageId().equals(practiceEntity.getPracticeIconStorageId())) {
            storageService.deleteStorageById(practiceEntity.getPracticeIconStorageId());
        }
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeBannerExternalUrl())
                && ObjectUtils.isNotEmpty(practiceRequestDto.getPracticeBannerStorageId()) && !practiceRequestDto
                        .getPracticeBannerStorageId().equals(practiceEntity.getPracticeBannerStorageId())) {
            storageService.deleteStorageById(practiceEntity.getPracticeBannerStorageId());
        }
        if (ObjectUtils.isEmpty(practiceEntity.getPracticeExternalUrl())
                && ObjectUtils.isNotEmpty(practiceRequestDto.getPracticeStorageId())
                && !practiceRequestDto.getPracticeStorageId().equals(practiceEntity.getPracticeStorageId())) {
            storageService.deleteStorageById(practiceEntity.getPracticeStorageId());
        }
        AppUtils.map(practiceRequestDto, practiceEntity);
        practiceEntity.setPracticeId(practiceId);
        if (ObjectUtils.isNotEmpty(practiceEntity.getPracticeStorageId())) {
            practiceEntity.setPracticeStorageId(practiceRequestDto.getPracticeStorageId());
            practiceEntity.setPracticeExternalUrl(null);
        } else {
            practiceEntity.setPracticeStorageId(null);
            practiceEntity.setPracticeExternalUrl(practiceRequestDto.getPracticeExternalUrl());
        }
        if (ObjectUtils.isNotEmpty(practiceEntity.getPracticeBannerStorageId())) {
            practiceEntity.setPracticeBannerStorageId(practiceRequestDto.getPracticeBannerStorageId());
            practiceEntity.setPracticeBannerExternalUrl(null);
        } else {
            practiceEntity.setPracticeBannerStorageId(null);
            practiceEntity.setPracticeBannerExternalUrl(practiceRequestDto.getPracticeBannerExternalUrl());
        }
        if (ObjectUtils.isNotEmpty(practiceEntity.getPracticeIconStorageId())) {
            practiceEntity.setPracticeIconStorageId(practiceRequestDto.getPracticeIconStorageId());
            practiceEntity.setPracticeIconExternalUrl(null);
        } else {
            practiceEntity.setPracticeIconStorageId(null);
            practiceEntity.setPracticeIconExternalUrl(practiceRequestDto.getPracticeIconExternalUrl());
        }
        if (ObjectUtils.isNotEmpty(practiceEntity.getTags())) {
            practiceEntity.setTags(AppUtils.writeValueAsString(practiceRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.PRACTICE_TAGS, practiceRequestDto.getTags());
        }
        return practiceRepository.save(practiceEntity);
    }

    /**
     * Retrieves a practice by its ID.
     *
     * @param practiceId
     *            the ID of the practice to retrieve
     * @return the PracticeEntity with the specified ID
     * @throws AppException
     *             if no practice is found with the given ID
     */
    public PracticeEntity getPracticeById(UUID practiceId) {
        return practiceRepository.findById(practiceId)
                .orElseThrow(AppUtils.Messages.PRACTICE_CATEGORY_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of practices based on the provided parameters.
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
     *            a keyword to search practices by name or description
     * @param categoryId
     *            the ID of the category to filter practices by (optional)
     * @return a Page containing PracticeEntity objects
     */
    public Page<PracticeEntity> getPractices(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword, UUID categoryId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());
        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> practiceRepository.searchDynamic(categoryId, keyword,
                    organizationService.getOrgIdsForMobile(), AppUtils.PracticeStatus.ACTIVE, pageable);
            case PORTAL_USER -> practiceRepository.searchDynamic(categoryId, keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> practiceRepository.searchDynamic(categoryId, keyword, null, null, pageable);
        };
    }

    /**
     * Deletes a practice by its ID.
     *
     * @param practiceId
     *            the ID of the practice to delete
     */
    public void delete(UUID practiceId) {
        PracticeEntity practiceEntity = getPracticeById(practiceId);
        Set<UUID> storageIds = Stream.of(practiceEntity.getPracticeId(), practiceEntity.getPracticeId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        practiceRepository.deleteById(practiceId);
    }

    /**
     * Changes the status of a practice.
     *
     * @param practiceId
     *            the ID of the practice to change status
     * @param status
     *            the new status to set
     */
    public void changePracticeStatus(UUID practiceId, AppUtils.PracticeStatus status) {
        PracticeEntity practiceEntity = getPracticeById(practiceId);
        practiceEntity.setPracticeStatus(status);
        practiceRepository.save(practiceEntity);
        if (AppUtils.PracticeStatus.ACTIVE.equals(status)) {
            sendPracticeNotification(getPracticeResponseDto(practiceEntity));
        }
    }

    // Practice User Methods

    /**
     * Adds or updates a practice user based on the provided request DTO.
     *
     * @param practiceUserRequestDto
     *            the request DTO containing practice user details
     * @return the saved PracticeUserEntity
     */
    public PracticeUserEntity updatePracticeUser(PracticeUserRequestDto practiceUserRequestDto) {
        PracticeUserEntity practiceUserEntity = getPracticeUserByPracticeIdUserId(
                practiceUserRequestDto.getPracticeId(), practiceUserRequestDto.getUserId());
        if (ObjectUtils.isEmpty(practiceUserEntity)) {
            practiceUserEntity = new PracticeUserEntity();
        }
        practiceUserEntity.setPracticeId(practiceUserRequestDto.getPracticeId());
        practiceUserEntity.setUserId(practiceUserRequestDto.getUserId());
        practiceUserEntity.setResumeTime(practiceUserRequestDto.getResumeTime());
        return practiceUserRepository.save(practiceUserEntity);
    }

    /**
     * Retrieves a practice user by its ID.
     *
     * @param practiceUserId
     *            the ID of the practice user to retrieve
     *
     * @throws AppException
     *             if no practice user is found with the given ID
     */
    public void changePracticeUserStatus(UUID practiceUserId, AppUtils.PracticeUserStatus status) {
        PracticeUserEntity practiceUserEntity = getPracticeUserById(practiceUserId);
        practiceUserEntity.setPracticeUserStatus(status);
        practiceUserRepository.save(practiceUserEntity);
    }

    /**
     * Changes the rating and comment for a practice user.
     *
     * @param practiceUserId
     *            the ID of the practice user to update
     * @param practiceUserRatingUpdateDto
     *            the DTO containing the new rating and comment
     */
    public void changeRatingAndComment(UUID practiceUserId, PracticeUserRatingUpdateDto practiceUserRatingUpdateDto) {
        PracticeUserEntity practiceUserEntity = getPracticeUserById(practiceUserId);
        boolean isRated = practiceUserEntity.getRating() != null;
        practiceUserEntity.setRating(practiceUserRatingUpdateDto.getRating());
        practiceUserEntity.setComments(practiceUserRatingUpdateDto.getComment());
        practiceUserRepository.save(practiceUserEntity);
        updatePracticeRating(practiceUserEntity.getPracticeId(), isRated);
    }

    /**
     * Retrieves a practice user by practice ID and user ID.
     *
     * @param practiceId
     *            the ID of the practice
     * @param isRated
     *            whether the practice user has been rated
     */
    private void updatePracticeRating(UUID practiceId, boolean isRated) {
        PracticeEntity practiceEntity = getPracticeById(practiceId);
        List<Float> ratings = practiceUserRepository.findNonZeroRatingsByPracticeId(practiceId);
        long ratingCount = isRated ? ratings.size() : ratings.size() + 1;
        practiceEntity.setRatingCount(ratingCount);
        practiceEntity.setRating(AppUtils.calculateRating(ratings, ratingCount));
        practiceRepository.save(practiceEntity);
    }

    /**
     * Retrieves a practice user by practice ID and user ID.
     *
     * @param practiceId
     *            the ID of the practice
     * @param userId
     *            the ID of the user
     * @return the PracticeUserEntity with the specified practice ID and user ID
     * @throws AppException
     *             if no practice user is found with the given IDs
     */
    private PracticeUserEntity getPracticeUserByPracticeIdUserId(UUID practiceId, UUID userId) {
        return practiceUserRepository.getByPracticeUserByPracticeIdAndUserId(practiceId, userId);
    }

    private Map<UUID, PracticeUserEntity> getProgramUsersByProgramIds(List<UUID> programUserIds) {
        UUID principalUserId = AppUtils.getPrincipalUserId();

        return programUserIds.stream()
                .map(practiceId -> practiceUserRepository.findByPracticeIdAndUserId(practiceId, principalUserId) // returns
                        .map(entity -> Map.entry(practiceId, entity)))
                .flatMap(Optional::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves a practice user by its ID.
     *
     * @param practiceUserId
     *            the ID of the practice user to retrieve
     * @return the PracticeUserEntity with the specified ID
     * @throws AppException
     *             if no practice user is found with the given ID
     */
    private PracticeUserEntity getPracticeUserById(UUID practiceUserId) {
        return practiceUserRepository.findById(practiceUserId)
                .orElseThrow(AppUtils.Messages.PRACTICE_USER_NOT_FOUND::getException);
    }

    /**
     * Retrieves the total number of practices based on the user type.
     *
     * @return the total number of practices
     */
    public Long getTotalPractices() {
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER -> practiceRepository.count();
            case PORTAL_USER -> practiceRepository.countByOrgId(AppUtils.getPrincipalOrgId());
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Retrieves the top 3 active practices for the organization.
     *
     * @return a list of the top 3 active PracticeEntity objects
     */
    public List<PracticeEntity> getTop3Practices() {
        return practiceRepository.getTop3ByOrgIdAndPracticeStatusOrderByCreatedAtDesc(AppUtils.getPrincipalOrgId(),
                AppUtils.PracticeStatus.ACTIVE);
    }

    /**
     * @param practiceEntity
     *            contains all practice data.
     * @return practiceResponseDto containing all data from Practices
     */
    public PracticeResponseDto getPracticeResponseDto(PracticeEntity practiceEntity) {
        PracticeResponseDto practiceResponseDto = AppUtils.map(practiceEntity, PracticeResponseDto.class);
        practiceResponseDto.setOrgName(organizationService.getOrgNameByOrgId(practiceEntity.getOrgId()));
        practiceResponseDto.setOrgIconStorageUrl(
                organizationService.getOrgIconStorageIdToSignedIconUrl(practiceEntity.getOrgId()));
        practiceResponseDto.setPracticeCategoryName(
                practiceCategoryService.getPracticeCategoryNameById(practiceEntity.getPracticeCategoryId()));
        practiceResponseDto.setCreatedByName(userService.getUserNameById(practiceEntity.getCreatedBy()));
        practiceResponseDto.setUpdatedByName(userService.getUserNameById(practiceEntity.getUpdatedBy()));
        practiceResponseDto
                .setPracticeIconStorageUrl(storageService.getStorageUrl(practiceEntity.getPracticeIconStorageId()));
        practiceResponseDto
                .setPracticeBannerStorageUrl(storageService.getStorageUrl(practiceEntity.getPracticeBannerStorageId()));
        practiceResponseDto.setPracticeStorageUrl(storageService.getStorageUrl(practiceEntity.getPracticeStorageId()));
        practiceResponseDto.setTags(AppUtils.readValue(practiceEntity.getTags(), new TypeReference<>() {
        }));
        return practiceResponseDto;
    }

    /**
     * Converts a list of PracticeEntity objects to a list of PracticeResponseDto
     * objects.
     *
     * @param practiceEntities
     *            the list of PracticeEntity objects to convert
     * @return a list of PracticeResponseDto objects
     */
    public List<PracticeResponseDto> toPracticeResponseDto(List<PracticeEntity> practiceEntities) {
        List<UUID> orgIds = (practiceEntities.stream().flatMap(practiceEntity -> Stream.of(practiceEntity.getOrgId()))
                .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(practiceEntities.stream()
                .flatMap(practiceEntity -> Stream.of(practiceEntity.getPracticeIconStorageId(),
                        practiceEntity.getPracticeBannerStorageId(), practiceEntity.getPracticeStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> userNamesByIds = userService.getUserData(practiceEntities);
        Map<UUID, String> orgNamesByIds = organizationService.getOrgNamesByIds(orgIds);
        Map<UUID, String> orgIconStorageUrlByIds = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        Map<UUID, String> categoryNamesByIds = practiceCategoryService.getCategoryNameIdByIds(
                practiceEntities.stream().flatMap(practiceEntity -> Stream.of(practiceEntity.getPracticeCategoryId()))
                        .filter(Objects::nonNull).distinct().toList());
        return practiceEntities.stream().map(practice -> {
            PracticeResponseDto dto = AppUtils.map(practice, PracticeResponseDto.class);
            dto.setPracticeCategoryName(categoryNamesByIds.get(practice.getPracticeCategoryId()));
            dto.setCreatedByName(userNamesByIds.get(practice.getCreatedBy()));
            dto.setUpdatedByName(userNamesByIds.get(practice.getUpdatedBy()));
            dto.setOrgName(orgNamesByIds.get(practice.getOrgId()));
            dto.setOrgIconStorageUrl(orgIconStorageUrlByIds.get(practice.getOrgId()));
            dto.setPracticeIconStorageUrl(signedStorageUrlByIds.get(practice.getPracticeIconStorageId()));
            dto.setPracticeStorageUrl(signedStorageUrlByIds.get(practice.getPracticeStorageId()));
            dto.setPracticeBannerStorageUrl(signedStorageUrlByIds.get(practice.getPracticeBannerStorageId()));
            dto.setTags(AppUtils.readValue(practice.getTags(), new TypeReference<>() {
            }));
            return dto;
        }).toList();
    }

    /**
     * Converts a list of PracticeEntity objects to a list of
     * PracticeMobileResponseDto objects.
     *
     * @param practiceEntities
     *            the list of PracticeEntity objects to convert
     * @return a list of PracticeResponseDto objects
     */
    public List<PracticeMobileResponseDto> toPracticeMobileResponseDto(List<PracticeEntity> practiceEntities) {
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(practiceEntities.stream()
                .flatMap(practiceEntity -> Stream.of(practiceEntity.getPracticeIconStorageId(),
                        practiceEntity.getPracticeBannerStorageId(), practiceEntity.getPracticeStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> categoryNamesByIds = practiceCategoryService.getCategoryNameIdByIds(
                practiceEntities.stream().flatMap(practiceEntity -> Stream.of(practiceEntity.getPracticeCategoryId()))
                        .filter(Objects::nonNull).distinct().toList());
        Map<UUID, PracticeUserEntity> practiceUserMap = getProgramUsersByProgramIds(
                practiceEntities.stream().flatMap(practiceEntity -> Stream.of(practiceEntity.getPracticeId()))
                        .filter(Objects::nonNull).distinct().toList());
        return practiceEntities.stream().map(practice -> {
            PracticeMobileResponseDto dto = AppUtils.map(practice, PracticeMobileResponseDto.class);
            dto.setPracticeCategoryName(categoryNamesByIds.get(practice.getPracticeCategoryId()));
            dto.setPracticeIconStorageUrl(signedStorageUrlByIds.get(practice.getPracticeIconStorageId()));
            dto.setPracticeBannerStorageUrl(signedStorageUrlByIds.get(practice.getPracticeBannerStorageId()));
            dto.setPracticeStorageUrl(signedStorageUrlByIds.get(practice.getPracticeStorageId()));
            if (practiceUserMap.containsKey(practice.getPracticeId())) {
                dto.setPracticeUserId(practiceUserMap.get(practice.getPracticeId()).getPracticeUserId());
                dto.setPracticeUserStatus(practiceUserMap.get(practice.getPracticeId()).getPracticeUserStatus());
            }
            dto.setTags(AppUtils.readValue(practice.getTags(), new TypeReference<>() {
            }));
            return dto;
        }).toList();
    }

    /**
     * Sends a notification for the given practice.
     *
     * @param practiceResponseDto
     *            the DTO containing practice details
     */
    private void sendPracticeNotification(PracticeResponseDto practiceResponseDto) {
        try {
            String title = practiceResponseDto.getPracticeName();
            String body = practiceResponseDto.getPracticeDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(practiceResponseDto.getPracticeBannerStorageUrl())) {
                imageUrl = practiceResponseDto.getPracticeBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(practiceResponseDto.getPracticeBannerExternalUrl())) {
                imageUrl = practiceResponseDto.getPracticeBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.PRACTICE, title, body, imageUrl,
                    practiceResponseDto.getPracticeId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }
}
