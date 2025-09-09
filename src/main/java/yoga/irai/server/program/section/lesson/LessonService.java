package yoga.irai.server.program.section.lesson;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.LessonMobileResponseDto;
import yoga.irai.server.program.ProgramEntity;
import yoga.irai.server.program.ProgramRepository;
import yoga.irai.server.program.ProgramService;
import yoga.irai.server.program.section.SectionEntity;
import yoga.irai.server.program.section.SectionRepository;
import yoga.irai.server.program.section.SectionService;
import yoga.irai.server.program.section.lesson.user.LessonUserEntity;
import yoga.irai.server.program.section.lesson.user.LessonUserRepository;
import yoga.irai.server.program.section.lesson.user.LessonUserRequestDto;
import yoga.irai.server.storage.StorageService;

@Service
@AllArgsConstructor
public class LessonService {
    private final UserService userService;
    private final StorageService storageService;
    private final SectionService sectionService;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final ProgramService programService;
    private final ProgramRepository programRepository;
    private final LessonUserRepository lessonUserRepository;

    /**
     * Adds a new lesson to the system.
     *
     * @param lessonRequestDto
     *            the details of the lesson to be added
     * @return the added LessonEntity
     * @throws AppException
     *             if a lesson with the same name already exists
     */
    public LessonEntity addLesson(@Valid LessonRequestDto lessonRequestDto) {
        if (lessonRepository.existsByLessonName(lessonRequestDto.getLessonName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        LessonEntity lessonEntity = AppUtils.map(lessonRequestDto, LessonEntity.class);
        Integer maxOrder = lessonRepository.findMaxLessonOrderBySectionId(lessonRequestDto.getSectionId());
        lessonEntity.setLessonOrder((maxOrder == null ? 1 : maxOrder + 1));
        SectionEntity sectionEntity = sectionService.getSectionById(lessonRequestDto.getSectionId());
        sectionEntity.setNumberOfLessons(sectionEntity.getNumberOfLessons() + 1);
        ProgramEntity programEntity = programService.getProgramById(sectionEntity.getProgramId());
        programEntity.setNumberOfLessons(programEntity.getNumberOfLessons() + 1);
        programEntity.setDuration(programEntity.getDuration() + lessonRequestDto.getDuration());
        sectionRepository.save(sectionEntity);
        programRepository.save(programEntity);
        return lessonRepository.save(lessonEntity);
    }

    /**
     * Updates an existing lesson in the system.
     *
     * @param lessonId
     *            the ID of the lesson to be updated
     * @param lessonRequestDto
     *            the new details of the lesson
     * @return the updated LessonEntity
     * @throws AppException
     *             if the lesson with the specified ID does not exist
     */
    public LessonEntity updateLesson(UUID lessonId, @Valid LessonRequestDto lessonRequestDto) {
        LessonEntity lessonEntity = getLessonById(lessonId);
        if (ObjectUtils.isEmpty(lessonEntity.getLessonExternalUrl())
                && ObjectUtils.isNotEmpty(lessonRequestDto.getLessonStorageId())
                && !lessonRequestDto.getLessonStorageId().equals(lessonEntity.getLessonStorageId())) {
            storageService.deleteStorageById(lessonEntity.getLessonStorageId());
        }
        Long oldDuration = lessonEntity.getDuration();
        AppUtils.map(lessonRequestDto, lessonEntity);
        lessonEntity.setSectionId(lessonId);
        if (ObjectUtils.isNotEmpty(lessonRequestDto.getLessonStorageId())) {
            lessonEntity.setLessonStorageId(lessonRequestDto.getLessonStorageId());
            lessonEntity.setLessonExternalUrl(null);
        } else {
            lessonEntity.setLessonStorageId(null);
            lessonEntity.setLessonExternalUrl(lessonRequestDto.getLessonExternalUrl());
        }
        ProgramEntity programEntity = programService
                .getProgramById(sectionService.getSectionById(lessonEntity.getSectionId()).getProgramId());
        programEntity.setDuration(programEntity.getDuration() - oldDuration + lessonRequestDto.getDuration());
        return lessonRepository.save(lessonEntity);
    }

    /**
     * Retrieves all lessons associated with a specific program section.
     *
     * @param sectionId
     *            the ID of the section for which to retrieve lessons
     * @return a list of LessonEntity objects associated with the specified section
     */
    public List<LessonEntity> getAllLessonByProgramId(UUID sectionId) {
        return lessonRepository.getAllBySectionId(sectionId);
    }

    /**
     * Retrieves a lesson by its ID.
     *
     * @param lessonId
     *            the ID of the lesson to retrieve
     * @return the LessonEntity associated with the specified ID
     * @throws AppException
     *             if no lesson with the specified ID exists
     */
    public LessonEntity getLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(AppUtils.Messages.LESSON_NOT_FOUND.getMessage()));
    }

    /**
     * Deletes a lesson by its ID.
     *
     * @param lessonId
     *            the ID of the lesson to delete
     * @throws AppException
     *             if no lesson with the specified ID exists
     */
    public void deleteLesson(UUID lessonId) {
        LessonEntity lessonEntity = getLessonById(lessonId);
        SectionEntity sectionEntity = sectionService.getSectionById(lessonEntity.getSectionId());
        sectionEntity.setNumberOfLessons(sectionEntity.getNumberOfLessons() - 1);
        ProgramEntity programEntity = programService.getProgramById(sectionEntity.getProgramId());
        programEntity.setNumberOfLessons(programEntity.getNumberOfLessons() - 1);
        programEntity.setDuration(programEntity.getDuration() - lessonEntity.getDuration());
        storageService.deleteStorageById(lessonEntity.getLessonStorageId());
        programRepository.save(programEntity);
        sectionRepository.save(sectionEntity);
        lessonRepository.delete(lessonEntity);
    }

    /**
     * Updates or creates a lesson user based on the provided request DTO.
     *
     * @param lessonUserRequestDto
     *            the DTO containing program user details
     * @return the saved ProgramUserEntity
     */
    public LessonUserEntity updateLessonUser(LessonUserRequestDto lessonUserRequestDto) {
        LessonUserEntity lessonUserEntity = getLessonUserByLessonIdUserId(lessonUserRequestDto.getLessonId(),
                lessonUserRequestDto.getUserId());
        if (ObjectUtils.isEmpty(lessonUserEntity)) {
            lessonUserEntity = new LessonUserEntity();
        }
        lessonUserEntity.setLessonId(lessonUserRequestDto.getLessonId());
        lessonUserEntity.setUserId(lessonUserRequestDto.getUserId());
        lessonUserEntity.setResumeTime(lessonUserRequestDto.getResumeTime());
        return lessonUserRepository.save(lessonUserEntity);
    }

    /**
     * Changes the status of a lesson user.
     *
     * @param lessonUserId
     *            the ID of the lesson user to update
     * @param status
     *            the new status to set
     */
    public void changeLessonUserStatus(UUID lessonUserId, AppUtils.LessonUserStatus status) {
        LessonUserEntity lessonUserEntity = getLessonUserById(lessonUserId);
        lessonUserEntity.setLessonUserStatus(status);
        lessonUserRepository.save(lessonUserEntity);
    }

    /**
     * Retrieves a lesson user by its ID.
     *
     * @param lessonUserId
     *            the ID of the program user to retrieve
     * @return the ProgramUserEntity if found
     * @throws AppException
     *             if the program user does not exist
     */
    private LessonUserEntity getLessonUserById(UUID lessonUserId) {
        return lessonUserRepository.findById(lessonUserId)
                .orElseThrow(AppUtils.Messages.LESSON_USER_NOT_FOUND::getException);
    }

    /**
     * Retrieves a lesson user by lesson ID and user ID.
     *
     * @param lessonId
     *            the ID of the lesson
     * @param userId
     *            the ID of the user
     * @return the LessonUserEntity if found
     */
    private LessonUserEntity getLessonUserByLessonIdUserId(UUID lessonId, UUID userId) {
        return lessonUserRepository.getByLessonIdAndUserId(lessonId, userId);
    }

    private Map<UUID, LessonUserEntity> getLessonUsersByLessonIds(List<UUID> programUserIds) {
        UUID principalUserId = AppUtils.getPrincipalUserId();

        return programUserIds.stream()
                .map(programId -> lessonUserRepository.findByLessonIdAndUserId(programId, principalUserId) // returns
                                                                                                            // Optional
                        .map(entity -> Map.entry(programId, entity)))
                .flatMap(Optional::stream) // keep only present
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Converts a list of LessonEntity objects to a list of LessonResponseDto
     * objects.
     *
     * @param lessonEntities
     *            the list of LessonEntity objects to convert
     * @return a list of LessonResponseDto objects
     */
    public List<LessonResponseDto> toLessonResponseDtos(List<LessonEntity> lessonEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(lessonEntities);
        return lessonEntities.stream().map(lessonEntity -> {
            LessonResponseDto lessonResponseDto = AppUtils.map(lessonEntity, LessonResponseDto.class);
            lessonResponseDto.setLessonStorageUrl(storageService.getStorageUrl(lessonEntity.getLessonStorageId()));
            lessonResponseDto.setCreatedByName(userNamesByIds.get(lessonEntity.getCreatedBy()));
            lessonResponseDto.setUpdatedByName(userNamesByIds.get(lessonEntity.getUpdatedBy()));
            return lessonResponseDto;
        }).toList();
    }

    /**
     * Converts a list of LessonEntity objects to a list of LessonMobileResponseDto
     * objects.
     *
     * @param lessonEntities
     *            the list of LessonEntity objects to convert
     * @return a list of LessonMobileResponseDto objects
     */
    public List<LessonMobileResponseDto> toLessonMobileResponseDtos(List<LessonEntity> lessonEntities) {
        Map<UUID, LessonUserEntity> programUserMap = getLessonUsersByLessonIds(
                lessonEntities.stream().flatMap(lessonEntity -> Stream.of(lessonEntity.getLessonId()))
                        .filter(Objects::nonNull).distinct().toList());
        return lessonEntities.stream().map(lessonEntity -> {
            LessonMobileResponseDto lessonMobileResponseDto = AppUtils.map(lessonEntity, LessonMobileResponseDto.class);
            if (programUserMap.containsKey(lessonEntity.getLessonId())) {
                lessonMobileResponseDto
                        .setLessonUserId(programUserMap.get(lessonEntity.getLessonId()).getLessonUserId());
                lessonMobileResponseDto
                        .setLessonUserStatus(programUserMap.get(lessonEntity.getLessonId()).getLessonUserStatus());
                lessonMobileResponseDto.setResumeTime(programUserMap.get(lessonEntity.getLessonId()).getResumeTime());
            }
            lessonMobileResponseDto
                    .setLessonStorageUrl(storageService.getStorageUrl(lessonEntity.getLessonStorageId()));
            return lessonMobileResponseDto;
        }).toList();
    }
}
