package yoga.irai.server.program;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.app.dto.TotalDto;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.program.section.SectionEntity;
import yoga.irai.server.program.section.SectionRequestDto;
import yoga.irai.server.program.section.SectionResponseDto;
import yoga.irai.server.program.section.SectionService;
import yoga.irai.server.program.section.lesson.LessonEntity;
import yoga.irai.server.program.section.lesson.LessonRequestDto;
import yoga.irai.server.program.section.lesson.LessonResponseDto;
import yoga.irai.server.program.section.lesson.LessonService;
import yoga.irai.server.storage.StorageService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramControllerTest {
    @Mock private LessonService lessonService;
    @Mock private ProgramService programService;
    @Mock private SectionService sectionService;
    @Mock private UserService userService;
    @Mock private StorageService storageService;
    private ProgramRequestDto  programRequestDto;
    private SectionRequestDto sectionRequestDto;
    private ProgramEntity programEntity;
    private SectionEntity sectionEntity;
    private ProgramResponseDto  programResponseDto;
    private LessonRequestDto lessonRequestDto;
    private LessonEntity lessonEntity;
    private UUID programId;
    private UUID sectionId;
    private UUID lessonId;
    @InjectMocks
    private ProgramController programController;
    @BeforeEach
    void setup() {
        lessonId = UUID.randomUUID();
        programId = UUID.randomUUID();
        sectionId = UUID.randomUUID();
        lessonEntity = LessonEntity.builder()
                .sectionId(sectionId)
                .build();
        lessonRequestDto = LessonRequestDto.builder()
                .sectionId(sectionId)
                .lessonName("Lesson 1")
                .duration(5L)
                .build();
        sectionRequestDto = SectionRequestDto.builder()
                .programId(programId)
                .sectionName("Section 1")
                .build();
        sectionEntity = SectionEntity.builder()
                .sectionId(UUID.randomUUID())
                .programId(programId)
                .sectionName("Section 1")
                .sectionDescription("Test")
                .numberOfLessons(1)
                .sectionOrder(2)
                .build();
        programRequestDto = ProgramRequestDto.builder()
                .programName("Program 1")
                .orgId(UUID.randomUUID())
                .build();
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .programName("Program 1")
                .build();
        programResponseDto = ProgramResponseDto.builder()
                .programId(programEntity.getProgramId())
                .programName(programEntity.getProgramName()).build();
    }
    @Test
    void addProgramTest(){
        when(programService.addProgram(programRequestDto)).thenReturn(programEntity);
        when(programService.getProgramResponseDto(programEntity)).thenReturn(programResponseDto);
        ResponseEntity<AppResponseDto<ProgramResponseDto>> response = programController.addProgram(programRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }
    @Test
    void updateProgramTest(){
        when(programService.updateProgram(programId, programRequestDto)).thenReturn(programEntity);
        when(programService.getProgramResponseDto(programEntity)).thenReturn(programResponseDto);
        ResponseEntity<AppResponseDto<ProgramResponseDto>> response = programController.updateProgram(programId , programRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());

    }
    @Test
    void getProgramTest(){
        when(programService.getProgramById(programId)).thenReturn(programEntity);
        when(programService.getProgramResponseDto(programEntity)).thenReturn(programResponseDto);
        ResponseEntity<AppResponseDto<ProgramResponseDto>> response = programController.getProgram(programId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void deleteProgramTest(){
        doNothing().when(programService).deleteProgram(programId);
        ResponseEntity<AppResponseDto<Void>> response = programController.deleteProgram(programId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }
    @Test
    void updateProgramStatusTest(){
        doNothing().when(programService).updateProgramStatus(programId, AppUtils.ProgramStatus.INACTIVE);
        ResponseEntity<AppResponseDto<Void>> response = programController.updateProgramStatus(programId, AppUtils.ProgramStatus.INACTIVE);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void addSectionTest(){
        when(sectionService.addSection(sectionRequestDto)).thenReturn(sectionEntity);
        ResponseEntity<AppResponseDto<SectionResponseDto>> response = programController.addSection(sectionRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void updateSectionTest(){
        when(sectionService.updateSection(sectionId, sectionRequestDto)).thenReturn(sectionEntity);
        ResponseEntity<AppResponseDto<SectionResponseDto>> response = programController.updateSection(sectionId , sectionRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getSectionTest(){
        when(sectionService.getSectionById(sectionId)).thenReturn(sectionEntity);
        ResponseEntity<AppResponseDto<SectionResponseDto>> response = programController.getSection(sectionId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getAllSectionsByProgramIdTest(){
        SectionResponseDto sectionResponseDto = new SectionResponseDto();
        sectionResponseDto.setSectionId(sectionEntity.getSectionId());
        sectionResponseDto.setSectionName(sectionEntity.getSectionName());
        when(sectionService.getAllSectionByProgramId(programId)).thenReturn(List.of(sectionEntity));
        when(sectionService.toSectionResponseDtos(List.of(sectionEntity))).thenReturn(List.of(sectionResponseDto));
        ResponseEntity<AppResponseDto<List<SectionResponseDto>>> response =  programController.getAllSectionsByProgramId(programId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void deleteSectionTest(){
        doNothing().when(sectionService).deleteSection(sectionId);
        ResponseEntity<AppResponseDto<Void>> response = programController.deleteSection(sectionId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());

    }

    @Test
    void addLessonTest(){
        when(lessonService.addLesson(lessonRequestDto)).thenReturn(lessonEntity);
        ResponseEntity<AppResponseDto<LessonResponseDto>> response = programController.addLesson(lessonRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void updateSectionTestDetails(){
        when(lessonService.updateLesson(lessonId, lessonRequestDto)).thenReturn(lessonEntity);
        ResponseEntity<AppResponseDto<LessonResponseDto>> response = programController.updateSection(lessonId , lessonRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void updateProgramStatusTestFlag(){
        doNothing().when(programService).updateProgramFlag(programId, AppUtils.ProgramFlag.TRENDING);
        ResponseEntity<AppResponseDto<Void>> response = programController.updateProgramStatus(programId , AppUtils.ProgramFlag.TRENDING);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getLessonTest(){
        when(lessonService.getLessonById(lessonId)).thenReturn(lessonEntity);
        ResponseEntity<AppResponseDto<LessonResponseDto>> response = programController.getLesson(lessonId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }
    @Test
    void deleteLessonTest(){
        doNothing().when(lessonService).deleteLesson(lessonId);
        ResponseEntity<AppResponseDto<Void>> response = programController.deleteLesson(lessonId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getPortalDashboardTest(){
        when(programService.getTotalPrograms()).thenReturn(5L);
        ResponseEntity<AppResponseDto<TotalDto>> response = programController.getPortalDashboard();
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getAllLessonsBySectionIdTest(){
        LessonResponseDto lessonResponseDto = new LessonResponseDto();
        lessonResponseDto.setLessonId(lessonEntity.getLessonId());
        lessonResponseDto.setLessonName(lessonEntity.getLessonName());
        when(lessonService.getAllLessonByProgramId(sectionId)).thenReturn(List.of(lessonEntity));
        when(lessonService.toLessonResponseDtos(List.of(lessonEntity))).thenReturn(List.of(lessonResponseDto));
        ResponseEntity<AppResponseDto<List<LessonResponseDto>>> response =  programController.getAllLessonsBySectionId(sectionId);
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }

    @Test
    void getProgramPageTest(){
        Page<ProgramEntity> programPage = new PageImpl<>(List.of(programEntity),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);
        when(programService.getPrograms(0, 10, "createdAt", Sort.Direction.DESC, "Test"))
                .thenReturn(programPage);
        when(programService.toProgramResponseDto(List.of(programEntity)))
                .thenReturn(List.of(programResponseDto));
        ResponseEntity<AppResponseDto<List<ProgramResponseDto>>> response = programController
                .getProgram(0 , 10 , "createdAt" , Sort.Direction.DESC , "Test");
        assert response.getStatusCode() == HttpStatus.OK;
        assertNotNull(response.getBody());
    }
}
