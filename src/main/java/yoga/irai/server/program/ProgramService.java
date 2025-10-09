package yoga.irai.server.program;

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
import yoga.irai.server.mobile.dto.ProgramMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.program.section.SectionEntity;
import yoga.irai.server.program.section.SectionRepository;
import yoga.irai.server.program.section.lesson.LessonEntity;
import yoga.irai.server.program.section.lesson.LessonRepository;
import yoga.irai.server.program.user.ProgramUserEntity;
import yoga.irai.server.program.user.ProgramUserRatingUpdateDto;
import yoga.irai.server.program.user.ProgramUserRepository;
import yoga.irai.server.program.user.ProgramUserRequestDto;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

/**
 * Service class for managing programs. Provides methods to add, update,
 * retrieve, and delete programs.
 */
@Service
@AllArgsConstructor
public class ProgramService {
    private final UserService userService;
    private final StorageService storageService;
    private final SettingService settingService;
    private final LessonRepository lessonRepository;
    private final ProgramRepository programRepository;
    private final SectionRepository sectionRepository;
    private final OrganizationService organizationService;
    private final ProgramUserRepository programUserRepository;
    private final NotificationService notificationService;

    /**
     * Adds a new program.
     *
     * @param programRequestDto
     *            the DTO containing program details
     * @return the saved ProgramEntity
     * @throws AppException
     *             if a program with the same name already exists
     */
    public ProgramEntity addProgram(@Valid ProgramRequestDto programRequestDto) {
        if (programRepository.existsByProgramName(programRequestDto.getProgramName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        ProgramEntity programEntity = AppUtils.map(programRequestDto, ProgramEntity.class);
        if (ObjectUtils.isNotEmpty(programRequestDto.getTags())) {
            programEntity.setTags(AppUtils.writeValueAsString(programRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.PROGRAM_TAGS, programRequestDto.getTags());
        }
        return programRepository.save(programEntity);
    }

    /**
     * Updates an existing program.
     *
     * @param programId
     *            the ID of the program to update
     * @param programRequestDto
     *            the DTO containing updated program details
     * @return the updated ProgramEntity
     * @throws AppException
     *             if the program does not exist
     */
    @Transactional
    public ProgramEntity updateProgram(UUID programId, @Valid ProgramRequestDto programRequestDto) {
        ProgramEntity programEntity = getProgramById(programId);
        if (ObjectUtils.isEmpty(programEntity.getProgramBannerExternalUrl())
                && ObjectUtils.isNotEmpty(programEntity.getProgramBannerStorageId())
                && !programRequestDto.getProgramBannerStorageId().equals(programEntity.getProgramBannerStorageId())) {
            storageService.deleteStorageById(programEntity.getProgramBannerStorageId());
        }
        AppUtils.map(programRequestDto, programEntity);
        programEntity.setProgramId(programId);
        if (ObjectUtils.isNotEmpty(programRequestDto.getProgramBannerStorageId())) {
            programEntity.setProgramBannerStorageId(programRequestDto.getProgramBannerStorageId());
            programEntity.setProgramBannerExternalUrl(null);
        } else {
            programEntity.setProgramBannerStorageId(null);
            programEntity.setProgramBannerExternalUrl(programRequestDto.getProgramBannerExternalUrl());
        }
        if (ObjectUtils.isNotEmpty(programEntity.getTags())) {
            programEntity.setTags(AppUtils.writeValueAsString(programRequestDto.getTags()));
            settingService.updateSetting(AppUtils.SettingName.PROGRAM_TAGS, programRequestDto.getTags());
        }
        return programRepository.save(programEntity);
    }

    /**
     * Retrieves a program by its ID.
     *
     * @param programId
     *            the ID of the program to retrieve
     * @return the ProgramEntity if found
     * @throws AppException
     *             if the program does not exist
     */
    public ProgramEntity getProgramById(UUID programId) {
        return programRepository.findById(programId).orElseThrow(AppUtils.Messages.PROGRAM_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of programs.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword
     * @return a Page containing ProgramEntity objects
     */
    public Page<ProgramEntity> getPrograms(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());

        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> programRepository.search(keyword, organizationService.getOrgIdsForMobile(),
                    AppUtils.ProgramStatus.ACTIVE, pageable);
            case PORTAL_USER -> programRepository.search(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> programRepository.search(keyword, null, null, pageable);
        };
    }

    /**
     * Deletes a program by its ID.
     *
     * @param programId
     *            the ID of the program to delete
     * @throws AppException
     *             if the program does not exist
     */
    public void deleteProgram(UUID programId) {
        ProgramEntity programEntity = getProgramById(programId);
        Set<UUID> storageIds = new HashSet<>();
        if (programEntity.getProgramBannerStorageId() != null) {
            storageIds.add(programEntity.getProgramBannerStorageId());
        }

        Set<UUID> lessonIds = new HashSet<>();
        programRepository.deleteById(programId);
        List<SectionEntity> sections = sectionRepository.getAllByProgramId(programId);
        for (SectionEntity sectionEntity : sections) {
            List<LessonEntity> lessons = lessonRepository.getAllBySectionId(sectionEntity.getSectionId());
            for (LessonEntity lessonEntity : lessons) {
                storageIds.add(lessonEntity.getLessonStorageId());
                lessonIds.add(lessonEntity.getLessonId());
            }
        }
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        lessonRepository.deleteAllById(lessonIds);
        sectionRepository.deleteAll(sections);
    }

    /**
     * Updates the status of a program.
     *
     * @param programId
     *            the ID of the program to update
     * @param status
     *            the new status to set
     * @throws AppException
     *             if the program does not exist
     */
    public void updateProgramStatus(UUID programId, AppUtils.ProgramStatus status) {
        ProgramEntity programEntity = getProgramById(programId);
        programEntity.setProgramStatus(status);
        programRepository.save(programEntity);
        if (AppUtils.ProgramStatus.ACTIVE.equals(status)) {
            sendProgramNotification(getProgramResponseDto(programEntity));
        }
    }

    /**
     * Updates the flag of a program.
     *
     * @param programId
     *            the ID of the program to update
     * @param flag
     *            the new flag to set
     * @throws AppException
     *             if the program does not exist
     */
    public void updateProgramFlag(UUID programId, AppUtils.ProgramFlag flag) {
        ProgramEntity programEntity = getProgramById(programId);
        programEntity.setFlag(flag);
        programRepository.save(programEntity);
    }

    /**
     * Retrieves the total number of programs based on the user type.
     *
     * @return the total number of programs
     */
    public Long getTotalPrograms() {
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER -> programRepository.count();
            case PORTAL_USER -> programRepository.countByOrgId(AppUtils.getPrincipalOrgId());
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Retrieves the top 3 active programs for the principal organization.
     *
     * @return a list of the top 3 active ProgramEntity objects
     */
    public List<ProgramEntity> getTop3Programs() {
        return programRepository.getTop3ByOrgIdAndProgramStatusOrderByCreatedAtDesc(AppUtils.getPrincipalOrgId(),
                AppUtils.ProgramStatus.ACTIVE);
    }

    public List<ProgramEntity> getViewedPrograms() {
        List<AppUtils.ProgramUserStatus> programStatus = List.of(AppUtils.ProgramUserStatus.IN_PROGRESS, AppUtils.ProgramUserStatus.STARTED);
        List<UUID> programIds = programUserRepository.getProgramUserEntityByUserIdAndProgramUserStatusIn(
                AppUtils.getPrincipalUserId(), programStatus).stream().map(ProgramUserEntity::getProgramId).toList();
        return programRepository.findAllById(programIds);
    }

    /**
     * Updates or creates a program user based on the provided request DTO.
     *
     * @param programUserRequestDto
     *            the DTO containing program user details
     * @return the saved ProgramUserEntity
     */
    public ProgramUserEntity updateProgramUser(ProgramUserRequestDto programUserRequestDto) {
        ProgramUserEntity programUserEntity = getProgramUserByProgramIdUserId(programUserRequestDto.getProgramId(),
                programUserRequestDto.getUserId());
        if (ObjectUtils.isEmpty(programUserEntity)) {
            programUserEntity = new ProgramUserEntity();
        }
        programUserEntity.setProgramId(programUserRequestDto.getProgramId());
        programUserEntity.setUserId(programUserRequestDto.getUserId());
        return programUserRepository.save(programUserEntity);
    }

    /**
     * Changes the status of a program user.
     *
     * @param programUserId
     *            the ID of the program user to update
     * @param status
     *            the new status to set
     */
    public void changeProgramUserStatus(UUID programUserId, AppUtils.ProgramUserStatus status) {
        ProgramUserEntity programUserEntity = getProgramUserById(programUserId);
        programUserEntity.setProgramUserStatus(status);
        programUserRepository.save(programUserEntity);
    }

    /**
     * Changes the rating and comment of a program user.
     *
     * @param programUserId
     *            the ID of the program user to update
     * @param programUserRatingUpdateDto
     *            the DTO containing the new rating and comment
     */
    public void changeRatingAndComment(UUID programUserId, ProgramUserRatingUpdateDto programUserRatingUpdateDto) {
        ProgramUserEntity programUserEntity = getProgramUserById(programUserId);
        boolean isRated = programUserEntity.getRating() != null;
        programUserEntity.setRating(programUserRatingUpdateDto.getRating());
        programUserEntity.setComments(programUserRatingUpdateDto.getComment());
        programUserRepository.save(programUserEntity);
        updateProgramRating(programUserEntity.getProgramId(), isRated);
    }

    /**
     * Updates the program rating based on the ratings of all users.
     *
     * @param programId
     *            the ID of the program to update
     * @param isRated
     *            whether the program was previously rated
     */
    private void updateProgramRating(UUID programId, boolean isRated) {
        ProgramEntity programEntity = getProgramById(programId);
        List<Float> ratings = programUserRepository.findNonZeroRatingsByProgramId(programId);
        long ratingCount = isRated ? ratings.size() : ratings.size() + 1;
        programEntity.setRatingCount(ratingCount);
        programEntity.setRating(AppUtils.calculateRating(ratings, ratingCount));
        programRepository.save(programEntity);
    }

    /**
     * Retrieves a program user by its ID.
     *
     * @param programUserId
     *            the ID of the program user to retrieve
     * @return the ProgramUserEntity if found
     * @throws AppException
     *             if the program user does not exist
     */
    private ProgramUserEntity getProgramUserById(UUID programUserId) {
        return programUserRepository.findById(programUserId)
                .orElseThrow(AppUtils.Messages.PROGRAM_USER_NOT_FOUND::getException);
    }

    private Map<UUID, ProgramUserEntity> getProgramUsersByProgramIds(List<UUID> programUserIds) {
        UUID principalUserId = AppUtils.getPrincipalUserId();

        return programUserIds.stream()
                .map(programId -> programUserRepository.findByProgramIdAndUserId(programId, principalUserId) // returns
                        .map(entity -> Map.entry(programId, entity)))
                .flatMap(Optional::stream) // keep only present
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Retrieves a program user by program ID and user ID.
     *
     * @param programId
     *            the ID of the program
     * @param userId
     *            the ID of the user
     * @return the ProgramUserEntity if found
     */
    private ProgramUserEntity getProgramUserByProgramIdUserId(UUID programId, UUID userId) {
        return programUserRepository.getByProgramIdAndUserId(programId, userId);
    }

    /**
     * Converts a list of ProgramEntity objects to a list of ProgramResponseDto
     * objects.
     *
     * @param programEntities
     *            the list of ProgramEntity objects to convert
     * @return a list of ProgramResponseDto objects
     */
    public List<ProgramResponseDto> toProgramResponseDto(List<ProgramEntity> programEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(programEntities);

        List<UUID> orgIds = (programEntities.stream().flatMap(programEntity -> Stream.of(programEntity.getOrgId()))
                .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(
                programEntities.stream().flatMap(programEntity -> Stream.of(programEntity.getProgramBannerStorageId()))
                        .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> orgNamesByIds = organizationService.getOrgNamesByIds(orgIds);
        Map<UUID, String> orgIconStorageUrlByIds = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        return programEntities.stream().map(programEntity -> {
            ProgramResponseDto programResponseDto = AppUtils.map(programEntity, ProgramResponseDto.class);
            programResponseDto.setCreatedByName(userNamesByIds.get(programEntity.getCreatedBy()));
            programResponseDto.setUpdatedByName(userNamesByIds.get(programEntity.getUpdatedBy()));
            programResponseDto.setOrgName(orgNamesByIds.get(programEntity.getOrgId()));
            programResponseDto.setOrgIconStorageUrl(orgIconStorageUrlByIds.get(programEntity.getOrgId()));
            programResponseDto.setOrgIconStorageUrl(signedStorageUrlByIds.get(programEntity.getOrgId()));
            programResponseDto
                    .setProgramBannerStorageUrl(signedStorageUrlByIds.get(programEntity.getProgramBannerStorageId()));
            if (ObjectUtils.isNotEmpty(programEntity.getFlag())) {
                programResponseDto.setFlag(programEntity.getFlag().getValue());
            }
            if (ObjectUtils.isNotEmpty(programEntity.getTags())) {
                programResponseDto.setTags(AppUtils.readValue(programEntity.getTags(), new TypeReference<>() {
                }));
            }
            return programResponseDto;
        }).toList();
    }

    /**
     * Converts a list of ProgramEntity objects to a list of
     * ProgramMobileResponseDto objects.
     *
     * @param programEntities
     *            the list of ProgramEntity objects to convert
     * @return a list of ProgramResponseDto objects
     */
    public List<ProgramMobileResponseDto> toProgramMobileResponseDto(List<ProgramEntity> programEntities) {
        Map<UUID, String> signedStorageUrlByIds = storageService.getSignedStorageUrlByIds(
                programEntities.stream().flatMap(programEntity -> Stream.of(programEntity.getProgramBannerStorageId()))
                        .filter(Objects::nonNull).distinct().toList());
        Map<UUID, ProgramUserEntity> programUserMap = getProgramUsersByProgramIds(
                programEntities.stream().flatMap(programEntity -> Stream.of(programEntity.getProgramId()))
                        .filter(Objects::nonNull).distinct().toList());
        return programEntities.stream().map(programEntity -> {
            ProgramMobileResponseDto programMobileResponseDto = AppUtils.map(programEntity,
                    ProgramMobileResponseDto.class);
            programMobileResponseDto
                    .setProgramBannerStorageUrl(signedStorageUrlByIds.get(programEntity.getProgramBannerStorageId()));
            if (programUserMap.containsKey(programEntity.getProgramId())) {
                programMobileResponseDto
                        .setProgramUserId(programUserMap.get(programEntity.getProgramId()).getProgramUserId());
                programMobileResponseDto
                        .setProgramUserStatus(programUserMap.get(programEntity.getProgramId()).getProgramUserStatus());
            }
            if (ObjectUtils.isNotEmpty(programEntity.getFlag())) {
                programMobileResponseDto.setFlag(programEntity.getFlag().getValue());
            }
            if (ObjectUtils.isNotEmpty(programEntity.getTags())) {
                programMobileResponseDto.setTags(AppUtils.readValue(programEntity.getTags(), new TypeReference<>() {
                }));
            }
            return programMobileResponseDto;
        }).toList();
    }

    /**
     * Maps a ProgramEntity to a ProgramResponseDto with additional details.
     *
     * @param programEntity
     *            the program audit to map
     * @return the mapped ProgramResponseDto
     */
    public ProgramResponseDto getProgramResponseDto(ProgramEntity programEntity) {
        ProgramResponseDto programResponseDto = AppUtils.map(programEntity, ProgramResponseDto.class);
        programResponseDto.setCreatedByName(userService.getUserNameById(programEntity.getCreatedBy()));
        programResponseDto.setUpdatedByName(userService.getUserNameById(programEntity.getUpdatedBy()));
        programResponseDto.setOrgName(organizationService.getOrgNameByOrgId(programEntity.getOrgId()));
        programResponseDto
                .setOrgIconStorageUrl(organizationService.getOrgIconStorageIdToSignedIconUrl(programEntity.getOrgId()));
        programResponseDto
                .setProgramBannerStorageUrl(storageService.getStorageUrl(programEntity.getProgramBannerStorageId()));
        if (ObjectUtils.isNotEmpty(programEntity.getFlag())) {
            programResponseDto.setFlag(programEntity.getFlag().getValue());
        }
        programResponseDto.setTags(AppUtils.readValue(programEntity.getTags(), new TypeReference<>() {
        }));
        return programResponseDto;
    }

    /**
     * Sends a notification for the given Program.
     *
     * @param programResponseDto
     *            the DTO containing program details
     */
    private void sendProgramNotification(ProgramResponseDto programResponseDto) {
        try {
            String title = programResponseDto.getProgramName();
            String body = programResponseDto.getProgramDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(programResponseDto.getProgramBannerStorageUrl())) {
                imageUrl = programResponseDto.getProgramBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(programResponseDto.getProgramBannerExternalUrl())) {
                imageUrl = programResponseDto.getProgramBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.PROGRAM, title, body, imageUrl,
                    programResponseDto.getProgramId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }
}
