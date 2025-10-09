package yoga.irai.server.program.section;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.SectionMobileResponseDto;
import yoga.irai.server.program.section.lesson.LessonEntity;
import yoga.irai.server.program.section.lesson.LessonRepository;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {
    @Mock
    private UserService userService;
    @Mock private LessonRepository lessonRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private StorageService storageService;
    @InjectMocks private SectionService sectionService;
    private SectionRequestDto  sectionRequestDto;
    private UUID programId;
    private UUID lessonId;
    private UUID sectionId;
    private SectionEntity sectionEntity;
    @BeforeEach
    void setUp() {
        programId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        sectionEntity = SectionEntity.builder()
                .sectionId(sectionId)
                .sectionName("Section 1")
                .sectionOrder(6)
                .build();
        sectionRequestDto = SectionRequestDto.builder()
                .sectionName("Section test")
                .programId(programId)
                .build();
    }
    @Test
    void addSection_ShouldThrowException_WhenNameExists() {
        when(sectionRepository.existsBySectionName("Section test")).thenReturn(true);
        AppException ex = assertThrows(AppException.class,
                () -> sectionService.addSection(sectionRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(sectionRepository, never()).save(any());
    }
    @Test
    void addSection_ShouldSetOrderTo1_WhenNoExistingSections() {
        SectionRequestDto request = new SectionRequestDto();
        request.setSectionName("New Section");
        request.setProgramId(programId);
        when(sectionRepository.existsBySectionName("New Section")).thenReturn(false);
        when(sectionRepository.findMaxSectionOrderByProgramId(programId)).thenReturn(null);
        SectionEntity savedEntity = new SectionEntity();
        savedEntity.setSectionName("New Section");
        savedEntity.setSectionOrder(1);
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(savedEntity);
        SectionEntity result = sectionService.addSection(request);
        assertEquals("New Section", result.getSectionName());
        assertEquals(1, result.getSectionOrder());
        verify(sectionRepository).save(any(SectionEntity.class));
    }

    @Test
    void addSection_ShouldSetOrderToMaxPlus1_WhenExistingSectionsExist() {
        when(sectionRepository.existsBySectionName("Section test")).thenReturn(false);
        when(sectionRepository.findMaxSectionOrderByProgramId(programId)).thenReturn(5);
        SectionEntity savedEntity = new SectionEntity();
        savedEntity.setSectionName("Section test");
        savedEntity.setSectionOrder(6);
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(savedEntity);
        SectionEntity result = sectionService.addSection(sectionRequestDto);
        assertEquals("Section test", result.getSectionName());
        assertEquals(6, result.getSectionOrder());
        verify(sectionRepository).save(any(SectionEntity.class));
    }
    @Test
    void updateSection_ShouldUpdateAndSaveEntity_WhenFound() {
        SectionService spyService = spy(sectionService);
        doReturn(sectionEntity).when(spyService).getSectionById(sectionId);
        SectionEntity savedEntity = new SectionEntity();
        savedEntity.setSectionId(sectionId);
        savedEntity.setSectionName("Updated Section");
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(savedEntity);
        SectionEntity result = spyService.updateSection(sectionId, sectionRequestDto);
        assertEquals(sectionId, result.getSectionId());
        assertEquals("Updated Section", result.getSectionName());
        verify(sectionRepository).save(sectionEntity);
    }

    @Test
    void updateSection_ShouldThrow_WhenSectionNotFound() {
        SectionService spyService = spy(sectionService);
        doThrow(new AppException("Section not found")).when(spyService).getSectionById(sectionId);
        AppException ex = assertThrows(AppException.class,
                () -> spyService.updateSection(sectionId, sectionRequestDto));
        assertEquals("Section not found", ex.getMessage());
        verify(sectionRepository, never()).save(any());
    }
    @Test
    void getSectionById_ShouldReturnEntity_WhenFound() {
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(sectionEntity));
        SectionEntity result = sectionService.getSectionById(sectionId);
        assertNotNull(result);
        assertEquals(sectionId, result.getSectionId());
        assertEquals("Section 1", result.getSectionName());
        verify(sectionRepository).findById(sectionId);
    }

    @Test
    void getSectionById_ShouldThrow_WhenNotFound() {
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class,
                () -> sectionService.getSectionById(sectionId));
        assertEquals(AppUtils.Messages.SECTION_NOT_FOUND.getMessage(), ex.getMessage());
        verify(sectionRepository).findById(sectionId);
    }
    @Test
    void getAllSectionByProgramId_ShouldReturnSections() {
        SectionEntity section2 = new SectionEntity();
        section2.setSectionId(UUID.randomUUID());
        section2.setSectionName("Section 2");
        List<SectionEntity> mockSections = List.of(sectionEntity, section2);
        when(sectionRepository.getAllByProgramId(programId)).thenReturn(mockSections);
        List<SectionEntity> result = sectionService.getAllSectionByProgramId(programId);
        assertEquals(2, result.size());
        assertEquals("Section 1", result.get(0).getSectionName());
        assertEquals("Section 2", result.get(1).getSectionName());
        verify(sectionRepository).getAllByProgramId(programId);
    }
    @Test
    void deleteSection_ShouldDeleteWithLessonsAndStorage() {
        UUID storageId = UUID.randomUUID();
        LessonEntity lessonEntity = new LessonEntity();
        lessonEntity.setLessonId(lessonId);
        lessonEntity.setLessonStorageId(storageId);
        SectionService spyService = spy(sectionService);
        doReturn(sectionEntity).when(spyService).getSectionById(sectionId);
        when(lessonRepository.getAllBySectionId(sectionId)).thenReturn(List.of(lessonEntity));
        spyService.deleteSection(sectionId);
        verify(storageService).deleteStorageByIds(Set.of(storageId));
        verify(sectionRepository).deleteById(sectionId);
        verify(lessonRepository).deleteAllById(Set.of(lessonId));
    }
    @Test
    void deleteSection_ShouldDeleteWithoutLessons() {
        SectionService spyService = spy(sectionService);
        doReturn(sectionEntity).when(spyService).getSectionById(sectionId);
        when(lessonRepository.getAllBySectionId(sectionId)).thenReturn(Collections.emptyList());
        spyService.deleteSection(sectionId);
        verify(storageService, never()).deleteStorageByIds(anySet());
        verify(sectionRepository).deleteById(sectionId);
        verify(lessonRepository).deleteAllById(Collections.emptySet());
    }
    @Test
    void toSectionResponseDto_ShouldMapEntitiesToDto() {
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        SectionEntity section = new SectionEntity();
        section.setSectionId(UUID.randomUUID());
        section.setSectionName("Test Section");
        section.setCreatedBy(createdBy);
        section.setUpdatedBy(updatedBy);
        List<SectionEntity> sectionEntities = List.of(section);
        Map<UUID, String> userNames = new HashMap<>();
        userNames.put(createdBy, "Creator User");
        userNames.put(updatedBy, "Updater User");
        when(userService.getUserData(sectionEntities)).thenReturn(userNames);
        List<SectionResponseDto> result = sectionService.toSectionResponseDtos(sectionEntities);
        assertThat(result).hasSize(1);
        SectionResponseDto dto = result.getFirst();
        assertThat(dto.getSectionId()).isEqualTo(section.getSectionId());
        assertThat(dto.getSectionName()).isEqualTo("Test Section");
        assertThat(dto.getCreatedByName()).isEqualTo("Creator User");
        assertThat(dto.getUpdatedByName()).isEqualTo("Updater User");
        verify(userService).getUserData(sectionEntities);
    }
    @Test
    void toSectionMobileResponseDto_ShouldMapEntitiesToMobileDto() {
        List<SectionEntity> sectionEntities = List.of(sectionEntity);
        List<SectionMobileResponseDto> result = sectionService.toSectionMobileResponseDtos(sectionEntities);
        assertThat(result).hasSize(1);
        SectionMobileResponseDto dto = result.getFirst();
        assertThat(dto.getSectionId()).isEqualTo(sectionEntity.getSectionId());
        assertThat(dto.getSectionName()).isEqualTo("Section 1");
    }
}
