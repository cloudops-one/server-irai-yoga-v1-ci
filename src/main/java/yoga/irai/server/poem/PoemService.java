package yoga.irai.server.poem;

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
import yoga.irai.server.mobile.dto.PoemMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

/**
 * This service handles operations related to poems in the application.
 */
@Service
@AllArgsConstructor
public class PoemService {
    private final UserService userService;
    private final StorageService storageService;
    private final SettingService settingService;
    private final PoemRepository poemRepository;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;

    /**
     * Adds a new poem.
     *
     * @param poemRequestDto
     *            the poem data transfer object containing poem details
     * @return the newly added PoemEntity
     */
    public PoemEntity addPoem(PoemRequestDto poemRequestDto) {
        if (poemRepository.existsByPoemName(poemRequestDto.getPoemName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        PoemEntity poemEntity = AppUtils.map(poemRequestDto, PoemEntity.class);
        if (ObjectUtils.isNotEmpty(poemEntity.getPoemTags())) {
            poemEntity.setPoemTags(AppUtils.writeValueAsString(poemRequestDto.getPoemTags()));
            settingService.updateSetting(AppUtils.SettingName.POEM_TAGS, poemRequestDto.getPoemTags());
        }
        return poemRepository.save(poemEntity);
    }

    /**
     * Updates an existing poem.
     *
     * @param poemId
     *            the ID of the poem to update
     * @param poemRequestDto
     *            the updated poem data transfer object
     * @return the updated PoemEntity
     */
    @Transactional
    public PoemEntity updatePoem(UUID poemId, @Valid PoemRequestDto poemRequestDto) {
        PoemEntity poemEntity = getPoemById(poemId);
        if (ObjectUtils.isEmpty(poemEntity.getPoemIconExternalUrl())
                && ObjectUtils.isNotEmpty(poemRequestDto.getPoemIconStorageId())
                && !poemRequestDto.getPoemIconStorageId().equals(poemEntity.getPoemIconStorageId())) {
            storageService.deleteStorageById(poemEntity.getPoemIconStorageId());
        }
        if (ObjectUtils.isEmpty(poemEntity.getPoemBannerExternalUrl())
                && ObjectUtils.isNotEmpty(poemRequestDto.getPoemBannerStorageId())
                && !poemRequestDto.getPoemBannerStorageId().equals(poemEntity.getPoemBannerStorageId())) {
            storageService.deleteStorageById(poemEntity.getPoemBannerStorageId());
        }
        if (ObjectUtils.isEmpty(poemEntity.getPoemExternalUrl())
                && ObjectUtils.isNotEmpty(poemRequestDto.getPoemStorageId())
                && !poemRequestDto.getPoemStorageId().equals(poemEntity.getPoemStorageId())) {
            storageService.deleteStorageById(poemEntity.getPoemStorageId());
        }
        AppUtils.map(poemRequestDto, poemEntity);
        poemEntity.setPoemId(poemId);
        if (ObjectUtils.isNotEmpty(poemRequestDto.getPoemStorageId())) {
            poemEntity.setPoemExternalUrl(null);
            poemEntity.setPoemStorageId(poemRequestDto.getPoemStorageId());
        } else {
            poemEntity.setPoemExternalUrl(poemEntity.getPoemExternalUrl());
            poemEntity.setPoemStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(poemRequestDto.getPoemIconStorageId())) {
            poemEntity.setPoemIconStorageId(poemRequestDto.getPoemIconStorageId());
            poemEntity.setPoemIconExternalUrl(null);
        } else {
            poemEntity.setPoemIconExternalUrl(poemRequestDto.getPoemIconExternalUrl());
            poemEntity.setPoemIconStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(poemRequestDto.getPoemBannerStorageId())) {
            poemEntity.setPoemBannerStorageId(poemRequestDto.getPoemBannerStorageId());
            poemEntity.setPoemBannerExternalUrl(null);
        } else {
            poemEntity.setPoemBannerExternalUrl(poemRequestDto.getPoemBannerExternalUrl());
            poemEntity.setPoemBannerStorageId(null);
        }
        if (ObjectUtils.isNotEmpty(poemEntity.getPoemTags())) {
            poemEntity.setPoemTags(AppUtils.writeValueAsString(poemRequestDto.getPoemTags()));
            settingService.updateSetting(AppUtils.SettingName.POEM_TAGS, poemRequestDto.getPoemTags());
        }
        return poemRepository.save(poemEntity);
    }

    /**
     * Retrieves a poem by its ID.
     *
     * @param poemId
     *            the ID of the poem to retrieve
     * @return the PoemEntity corresponding to the given ID
     */
    public PoemEntity getPoemById(UUID poemId) {
        return poemRepository.findById(poemId).orElseThrow(AppUtils.Messages.POEM_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of poems, optionally filtered by keyword.
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
     *            an optional keyword to filter poems by title or content
     * @return a Page containing PoemEntity objects
     */
    public Page<PoemEntity> getPoems(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());
        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> poemRepository.search(keyword, organizationService.getOrgIdsForMobile(),
                    AppUtils.PoemStatus.ACTIVE, pageable);
            case PORTAL_USER -> poemRepository.search(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> poemRepository.search(keyword, null, null, pageable);
        };
    }

    /**
     * Deletes a poem by its ID.
     *
     * @param poemId
     *            the ID of the poem to delete
     */
    public void deletePoem(UUID poemId) {
        PoemEntity poemEntity = getPoemById(poemId);
        Set<UUID> storageIds = Stream.of(poemEntity.getPoemIconStorageId(), poemEntity.getPoemBannerStorageId(),
                poemEntity.getPoemStorageId()).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        poemRepository.deleteById(poemId);
    }

    /**
     * Changes the status of a poem.
     *
     * @param poemId
     *            the ID of the poem to change status
     * @param status
     *            the new status to set for the poem
     */
    public void changePoemStatus(UUID poemId, AppUtils.PoemStatus status) {
        PoemEntity poemEntity = getPoemById(poemId);
        poemEntity.setPoemStatus(status);
        poemRepository.save(poemEntity);
        if (AppUtils.PoemStatus.ACTIVE.equals(status)) {
            sendPoemNotification(getPoemResponseDto(poemEntity));
        }
    }

    /**
     * Updates the view count of a poem.
     *
     * @param poemId
     *            the ID of the poem to update
     */
    public void updateViewCount(UUID poemId) {
        PoemEntity poemEntity = getPoemById(poemId);
        poemEntity.setPoemViews(poemEntity.getPoemViews() + 1);
        poemRepository.save(poemEntity);
    }

    /**
     * Retrieves the total number of poems based on the user type.
     *
     * @return the total number of poems
     */
    public Long getTotalPoems() {
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER -> poemRepository.count();
            case PORTAL_USER -> poemRepository.countByOrgId(AppUtils.getPrincipalOrgId());
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Retrieves the top 3 poems based on the organization ID and poem status.
     *
     * @return a list of the top 3 PoemEntity objects
     */
    public List<PoemEntity> getTop3Poems() {
        return poemRepository.getTop3ByOrgIdAndPoemStatusOrderByCreatedAtDesc(AppUtils.getPrincipalOrgId(),
                AppUtils.PoemStatus.ACTIVE);
    }

    /**
     * Sends a notification for the given Poem.
     *
     * @param poemResponseDto
     *            the DTO containing poem details
     */
    private void sendPoemNotification(PoemResponseDto poemResponseDto) {
        try {
            String title = poemResponseDto.getPoemName();
            String body = poemResponseDto.getPoemDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(poemResponseDto.getPoemBannerStorageUrl())) {
                imageUrl = poemResponseDto.getPoemBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(poemResponseDto.getPoemBannerExternalUrl())) {
                imageUrl = poemResponseDto.getPoemBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.POEM, title, body, imageUrl,
                    poemResponseDto.getPoemId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * Converts a single PoemEntity to a PoemResponseDto.
     *
     * @param poemEntity
      *            the PoemEntity to convert to a response DTO
     * @return the converted PoemResponseDto
     */
    public PoemResponseDto getPoemResponseDto(PoemEntity poemEntity) {
        PoemResponseDto poemResponseDto = AppUtils.map(poemEntity, PoemResponseDto.class);
        poemResponseDto.setPoemStorageUrl(storageService.getStorageUrl(poemEntity.getPoemStorageId()));
        poemResponseDto.setPoemIconStorageUrl(storageService.getStorageUrl(poemEntity.getPoemIconStorageId()));
        poemResponseDto.setPoemBannerStorageUrl(storageService.getStorageUrl(poemEntity.getPoemBannerStorageId()));
        poemResponseDto.setCreatedByName(userService.getUserNameById(poemEntity.getCreatedBy()));
        poemResponseDto.setUpdatedByName(userService.getUserNameById(poemEntity.getUpdatedBy()));
        poemResponseDto.setPoemTags(AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
        }));
        return poemResponseDto;
    }

    /**
     * Converts a list of PoemEntity objects to a list of PoemResponseDto objects.
     *
     * @param poems
     *            the list of PoemEntity objects to convert
     * @return a list of PoemResponseDto objects
     */
    public List<PoemResponseDto> toPoemResponseDto(List<PoemEntity> poems) {
        Map<UUID, String> userNamesByIds = userService.getUserNamesByIds(
                poems.stream().flatMap(poemEntity -> Stream.of(poemEntity.getCreatedBy(), poemEntity.getUpdatedBy()))
                        .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(poems
                .stream().flatMap(poemEntity -> Stream.of(poemEntity.getPoemBannerStorageId(),
                        poemEntity.getPoemIconStorageId(), poemEntity.getPoemStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        return poems.stream().map(poemEntity -> {
            PoemResponseDto poemResponseDto = AppUtils.map(poemEntity, PoemResponseDto.class);
            poemResponseDto.setCreatedByName(userNamesByIds.get(poemEntity.getCreatedBy()));
            poemResponseDto.setUpdatedByName(userNamesByIds.get(poemEntity.getUpdatedBy()));
            poemResponseDto.setPoemStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemStorageId()));
            poemResponseDto.setPoemIconStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemIconStorageId()));
            poemResponseDto.setPoemBannerStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemBannerStorageId()));
            poemResponseDto.setPoemTags(AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
            }));
            return poemResponseDto;
        }).toList();
    }

    /**
     * Converts a list of PoemEntity objects to a list of PoemResponseDto objects.
     *
     * @param poems
     *            the list of PoemEntity objects to convert
     * @return a list of PoemResponseDto objects
     */
    public List<PoemMobileResponseDto> toPoemMobileResponseDto(List<PoemEntity> poems) {

        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(poems
                .stream().flatMap(poemEntity -> Stream.of(poemEntity.getPoemBannerStorageId(),
                        poemEntity.getPoemIconStorageId(), poemEntity.getPoemStorageId()))
                .filter(Objects::nonNull).distinct().toList());
        return poems.stream().map(poemEntity -> {
            PoemMobileResponseDto poemMobileResponseDto = AppUtils.map(poemEntity, PoemMobileResponseDto.class);
            poemMobileResponseDto.setPoemIconStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemIconStorageId()));
            poemMobileResponseDto.setPoemStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemStorageId()));
            poemMobileResponseDto
                    .setPoemBannerStorageUrl(signedStorageUrlByIds.get(poemEntity.getPoemBannerStorageId()));
            poemMobileResponseDto.setPoemTags(AppUtils.readValue(poemEntity.getPoemTags(), new TypeReference<>() {
            }));
            return poemMobileResponseDto;
        }).toList();
    }
}
