package yoga.irai.server.program.section.lesson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonUserRepository lessonUserRepository;
    @Mock
    private SectionService sectionService;
    @Mock
    private UserService userService;
    @Mock
    private ProgramService programService;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private LessonService lessonService;

    private LessonRequestDto lessonRequestDto;
    private LessonEntity lessonEntity;
    private LessonUserEntity  lessonUserEntity;
    private SectionEntity sectionEntity;
    private ProgramEntity programEntity;
    private LessonUserRequestDto lessonUserRequestDto;

    private UUID sectionId;
    private UUID userId;
    private UUID programId;
    private UUID lessonId;

    @BeforeEach
    void setup() {
        sectionId = UUID.randomUUID();
        programId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        userId  = UUID.randomUUID();
        lessonUserEntity = LessonUserEntity.builder()
                .lessonUserId(lessonId)
                .userId(userId)
                .lessonId(lessonId)
                .lessonUserStatus(AppUtils.LessonUserStatus.STARTED)
                .build();
        lessonUserRequestDto = LessonUserRequestDto.builder()
                .lessonId(lessonId)
                .userId(userId)
                .resumeTime(30L)
                .build();
        lessonRequestDto = LessonRequestDto.builder()
                .lessonName("Lesson 1")
                .lessonStorageId(UUID.randomUUID())
                .sectionId(sectionId)
                .duration(10L)
                .build();
        lessonEntity = LessonEntity.builder()
                .lessonId(UUID.randomUUID())
                .lessonName("Lesson 1")
                .sectionId(sectionId)
                .lessonStorageId(UUID.randomUUID())
                .lessonOrder(1)
                .duration(50L)
                .build();
        sectionEntity = SectionEntity.builder()
                .sectionId(sectionId)
                .programId(programId)
                .numberOfLessons(1)
                .build();
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .numberOfLessons(5)
                .duration(50L)
                .build();
    }

    @Test
    void addLessonTest() {
        when(lessonRepository.existsByLessonName("Lesson 1")).thenReturn(false);
        when(lessonRepository.findMaxLessonOrderBySectionId(sectionId)).thenReturn(1);
        when(sectionService.getSectionById(sectionId)).thenReturn(sectionEntity);
        when(programService.getProgramById(programId)).thenReturn(programEntity);
        when(lessonRepository.save(any(LessonEntity.class))).thenReturn(lessonEntity);
        LessonEntity result = lessonService.addLesson(lessonRequestDto);
        assertNotNull(result);
        assertEquals("Lesson 1", result.getLessonName());
        verify(sectionRepository).save(sectionEntity);
        verify(programRepository).save(programEntity);
        verify(lessonRepository).save(any(LessonEntity.class));
        assertEquals(2, sectionEntity.getNumberOfLessons());
        assertEquals(6, programEntity.getNumberOfLessons());
        assertEquals(60L, programEntity.getDuration());
    }
    @Test
    void addLessonSetLessonOrderTest() {
        when(lessonRepository.existsByLessonName("Lesson 1")).thenReturn(false);
        when(lessonRepository.findMaxLessonOrderBySectionId(sectionId)).thenReturn(null);
        when(sectionService.getSectionById(sectionId)).thenReturn(sectionEntity);
        when(programService.getProgramById(programId)).thenReturn(programEntity);
        when(lessonRepository.save(any(LessonEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(sectionEntity);
        when(programRepository.save(any(ProgramEntity.class))).thenReturn(programEntity);
        LessonEntity result = lessonService.addLesson(lessonRequestDto);
        assertEquals(1, result.getLessonOrder());
        when(lessonRepository.findMaxLessonOrderBySectionId(sectionId)).thenReturn(5);
        LessonEntity result2 = lessonService.addLesson(lessonRequestDto);
        assertEquals(6, result2.getLessonOrder());
    }

    @Test
    void addLessonException() {
        when(lessonRepository.existsByLessonName("Lesson 1")).thenReturn(true);
        assertThrows(AppException.class, () -> lessonService.addLesson(lessonRequestDto));
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateLesson_shouldUpdateDurationAndStorage() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lessonEntity));
        when(sectionService.getSectionById(any(UUID.class))).thenReturn(sectionEntity);
        when(programService.getProgramById(programId)).thenReturn(programEntity);
        when(lessonRepository.save(any(LessonEntity.class))).thenAnswer(i -> i.getArgument(0));
        LessonEntity result = lessonService.updateLesson(lessonId, lessonRequestDto);
        assertEquals(10L, result.getDuration());
        assertEquals(10L, programEntity.getDuration());
        verify(storageService).deleteStorageById(any());
        verify(lessonRepository).save(result);
        verify(lessonRepository, times(1)).save(lessonEntity);
    }

    @Test
    void getAllLessonByProgramIdTest() {
        when(lessonRepository.getAllBySectionId(sectionId))
                .thenReturn(List.of(lessonEntity));
        List<LessonEntity> result = lessonService.getAllLessonByProgramId(sectionId);
        assertEquals(1, result.size());
        assertEquals("Lesson 1", result.getFirst().getLessonName());
        verify(lessonRepository, times(1)).getAllBySectionId(sectionId);
    }
    @Test
    void deleteLessonTest() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lessonEntity));
        when(sectionService.getSectionById(sectionId)).thenReturn(sectionEntity);
        when(programService.getProgramById(programId)).thenReturn(programEntity);
        lessonService.deleteLesson(lessonId);
        assertEquals(0, sectionEntity.getNumberOfLessons());
        assertEquals(4, programEntity.getNumberOfLessons());
        assertEquals(0L, programEntity.getDuration());
        verify(storageService).deleteStorageById(lessonEntity.getLessonStorageId());
        verify(programRepository).save(programEntity);
        verify(sectionRepository).save(sectionEntity);
        verify(lessonRepository).delete(lessonEntity);
    }
    @Test
    void updateLessonUser_existingUser_shouldUpdate() {
        when(lessonUserRepository.getByLessonIdAndUserId(lessonId, userId)).thenReturn(lessonUserEntity);
        when(lessonUserRepository.save(any(LessonUserEntity.class))).thenAnswer(i -> i.getArgument(0));
        LessonUserEntity result = lessonService.updateLessonUser(lessonUserRequestDto);
        assertNotNull(result);
        assertEquals(lessonId, result.getLessonId());
        assertEquals(userId, result.getUserId());
        assertEquals(30L, result.getResumeTime());
        verify(lessonUserRepository).save(any(LessonUserEntity.class));
    }

    @Test
    void updateLessonUser_newUser_shouldCreate() {
        when(lessonUserRepository.getByLessonIdAndUserId(lessonId, userId)).thenReturn(null);
        when(lessonUserRepository.save(any(LessonUserEntity.class))).thenAnswer(i -> i.getArgument(0));
        LessonUserEntity result = lessonService.updateLessonUser(lessonUserRequestDto);
        assertNotNull(result);
        assertEquals(lessonId, result.getLessonId());
        assertEquals(userId, result.getUserId());
        assertEquals(30L, result.getResumeTime());
        verify(lessonUserRepository).save(any(LessonUserEntity.class));
    }
    @Test
    void changeLessonUserStatus_ShouldUpdateStatus() {
        when(lessonUserRepository.findById(lessonId)).thenReturn(Optional.of(lessonUserEntity));
        lessonService.changeLessonUserStatus(lessonId, AppUtils.LessonUserStatus.COMPLETE);
        assert lessonUserEntity.getLessonUserStatus() == AppUtils.LessonUserStatus.COMPLETE;
        verify(lessonUserRepository).save(lessonUserEntity);
    }
    @Test
    void toLessonMobileResponseDto_ShouldMapEntitiesToDto() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserPrincipalEntity principal = mock(UserPrincipalEntity.class);
        UserEntity userEntity = mock(UserEntity.class);
        when(userEntity.getUserId()).thenReturn(UUID.randomUUID());
        when(principal.user()).thenReturn(userEntity);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(storageService.getStorageUrl(lessonEntity.getLessonStorageId()))
                .thenReturn("signedLessonUrl");
        List<LessonMobileResponseDto> lessonMobileResponseDto = lessonService.toLessonMobileResponseDtos(List.of(lessonEntity));
        assertNotNull(lessonMobileResponseDto);
        assertEquals(1, lessonMobileResponseDto.size());
        LessonMobileResponseDto dto = lessonMobileResponseDto.getFirst();
        assertEquals("Lesson 1", dto.getLessonName());
        assertEquals("signedLessonUrl", dto.getLessonStorageUrl());
        assertNull(dto.getLessonUserId());
        assertNull(dto.getLessonUserStatus());
        assertNull(dto.getResumeTime());
        verify(storageService, times(1)).getStorageUrl(lessonEntity.getLessonStorageId());
    }
    @Test
    void testToLessonResponseDtoTest() {
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UUID storageId = UUID.randomUUID();
        LessonEntity lessonEntity = new LessonEntity();
        lessonEntity.setCreatedBy(createdBy);
        lessonEntity.setUpdatedBy(updatedBy);
        lessonEntity.setLessonStorageId(storageId);
        List<LessonEntity> lessonEntities = List.of(lessonEntity);
        Map<UUID, String> userNamesByIds = new HashMap<>();
        userNamesByIds.put(createdBy, "Hilton");
        userNamesByIds.put(updatedBy, "Paul");
        when(userService.getUserData(lessonEntities)).thenReturn(userNamesByIds);
        when(storageService.getStorageUrl(storageId)).thenReturn("https://www.google.com");
        List<LessonResponseDto> lessonResponseDto = lessonService.toLessonResponseDtos(lessonEntities);
        assertEquals(1, lessonResponseDto.size());
        LessonResponseDto dto = lessonResponseDto.getFirst();
        assertEquals("Hilton", dto.getCreatedByName());
        assertEquals("Paul", dto.getUpdatedByName());
        assertEquals("https://www.google.com", dto.getLessonStorageUrl());
    }
}
