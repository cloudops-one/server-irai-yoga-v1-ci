package yoga.irai.server.program.section;

import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.SectionMobileResponseDto;
import yoga.irai.server.program.section.lesson.LessonEntity;
import yoga.irai.server.program.section.lesson.LessonRepository;
import yoga.irai.server.storage.StorageService;

/**
 * Service class for managing sections within a program. Provides methods to
 * add, update, retrieve, and delete sections.
 */
@Service
@AllArgsConstructor
public class SectionService {
    private final UserService userService;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final StorageService storageService;

    /**
     * Adds a new section to a program.
     *
     * @param sectionRequestDto
     *            the DTO containing section details
     * @return the saved SectionEntity
     * @throws AppException
     *             if a section with the same name already exists
     */
    public SectionEntity addSection(@Valid SectionRequestDto sectionRequestDto) {
        if (sectionRepository.existsBySectionName(sectionRequestDto.getSectionName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        SectionEntity sectionEntity = AppUtils.map(sectionRequestDto, SectionEntity.class);
        Integer maxOrder = sectionRepository.findMaxSectionOrderByProgramId(sectionRequestDto.getProgramId());
        sectionEntity.setSectionOrder((maxOrder == null ? 1 : maxOrder + 1));
        return sectionRepository.save(sectionEntity);
    }

    /**
     * Updates an existing section.
     *
     * @param sectionId
     *            the ID of the section to update
     * @param sectionRequestDto
     *            the DTO containing updated section details
     * @return the updated SectionEntity
     * @throws AppException
     *             if the section is not found
     */
    public SectionEntity updateSection(UUID sectionId, @Valid SectionRequestDto sectionRequestDto) {
        SectionEntity sectionEntity = getSectionById(sectionId);
        AppUtils.map(sectionRequestDto, sectionEntity);
        sectionEntity.setSectionId(sectionId);
        return sectionRepository.save(sectionEntity);
    }

    /**
     * Retrieves a section by its ID.
     *
     * @param sectionId
     *            the ID of the section to retrieve
     * @return the SectionEntity with the specified ID
     * @throws AppException
     *             if the section is not found
     */
    public SectionEntity getSectionById(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(AppUtils.Messages.SECTION_NOT_FOUND.getMessage()));
    }

    /**
     * Retrieves all sections associated with a specific program ID.
     *
     * @param programId
     *            the ID of the program to filter sections
     * @return a list of SectionEntity matching the program ID
     */
    public List<SectionEntity> getAllSectionByProgramId(UUID programId) {
        return sectionRepository.getAllByProgramId(programId);
    }

    /**
     * Deletes a section by its ID. Also deletes all lessons associated with the
     * section.
     *
     * @param sectionId
     *            the ID of the section to delete
     */
    public void deleteSection(UUID sectionId) {
        SectionEntity sectionEntity = getSectionById(sectionId);
        Set<UUID> storageIds = new HashSet<>();
        Set<UUID> lessonIds = new HashSet<>();
        List<LessonEntity> lessons = lessonRepository.getAllBySectionId(sectionEntity.getSectionId());
        for (LessonEntity lessonEntity : lessons) {
            storageIds.add(lessonEntity.getLessonStorageId());
            lessonIds.add(lessonEntity.getLessonId());
        }
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        sectionRepository.deleteById(sectionId);
        lessonRepository.deleteAllById(lessonIds);
    }

    /**
     * Converts a list of SectionEntity to a list of SectionResponseDto.
     *
     * @param sectionEntities
     *            the list of SectionEntity to convert
     * @return a list of SectionResponseDto
     */
    public List<SectionResponseDto> toSectionResponseDtos(List<SectionEntity> sectionEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(sectionEntities);

        return sectionEntities.stream().map(sectionEntity -> {
            SectionResponseDto sectionResponseDto = AppUtils.map(sectionEntity, SectionResponseDto.class);
            sectionResponseDto.setCreatedByName(userNamesByIds.get(sectionEntity.getCreatedBy()));
            sectionResponseDto.setUpdatedByName(userNamesByIds.get(sectionEntity.getUpdatedBy()));

            return sectionResponseDto;
        }).toList();

    }

    /**
     * Converts a list of SectionEntity to a list of SectionMobileResponseDto.
     *
     * @param sectionEntities
     *            the list of SectionEntity to convert
     * @return a list of SectionMobileResponseDto
     */
    public List<SectionMobileResponseDto> toSectionMobileResponseDtos(List<SectionEntity> sectionEntities) {
        return sectionEntities.stream()
                .map(sectionEntity -> AppUtils.map(sectionEntity, SectionMobileResponseDto.class)).toList();

    }
}
