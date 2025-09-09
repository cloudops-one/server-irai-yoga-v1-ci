package yoga.irai.server.program;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

/**
 * Controller for managing programs, including creation, retrieval, updating,
 * and deletion. Provides endpoints to handle program-related operations.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/program")
@Tag(name = "Program Management", description = "APIs for managing programs, including creation, retrieval, updating, and deletion.")
public class ProgramController {
    private final UserService userService;
    private final LessonService lessonService;
    private final ProgramService programService;
    private final StorageService storageService;
    private final SectionService sectionService;

    /**
     * Adds a new program.
     *
     * @param programRequestDto
     *            the details of the program to be added
     * @return ResponseEntity containing the added program details
     */
    @PostMapping
    @Operation(summary = "Add a new program", description = "Creates a new program with the provided details. Returns the created program.")
    public ResponseEntity<AppResponseDto<ProgramResponseDto>> addProgram(
            @RequestBody @Valid ProgramRequestDto programRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<ProgramResponseDto> builder = AppResponseDto.builder();
        ProgramEntity programEntity = programService.addProgram(programRequestDto);
        ProgramResponseDto programResponseDto = programService.getProgramResponseDto(programEntity);
        return ResponseEntity
                .ok(builder.data(programResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing program.
     *
     * @param programId
     *            the ID of the program to be updated
     * @param programRequestDto
     *            the new details of the program
     * @return ResponseEntity containing the updated program details
     */
    @PutMapping("/{programId}")
    @Operation(summary = "Update an existing program", description = "Updates the details of an existing program identified by the programId. Returns the updated program.")
    public ResponseEntity<AppResponseDto<ProgramResponseDto>> updateProgram(@PathVariable UUID programId,
            @RequestBody @Valid ProgramRequestDto programRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<ProgramResponseDto> builder = AppResponseDto.builder();
        ProgramEntity programEntity = programService.updateProgram(programId, programRequestDto);
        ProgramResponseDto programResponseDto = programService.getProgramResponseDto(programEntity);
        return ResponseEntity
                .ok(builder.data(programResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a program by its ID.
     *
     * @param programId
     *            the ID of the program to be retrieved
     * @return ResponseEntity containing the program details
     */
    @GetMapping("/{programId}")
    @Operation(summary = "Get a program by ID", description = "Retrieves the details of a program identified by the programId. Returns the program details.")
    public ResponseEntity<AppResponseDto<ProgramResponseDto>> getProgram(@PathVariable UUID programId) {
        AppResponseDto.AppResponseDtoBuilder<ProgramResponseDto> builder = AppResponseDto.builder();
        ProgramEntity programEntity = programService.getProgramById(programId);
        ProgramResponseDto programResponseDto = programService.getProgramResponseDto(programEntity);
        return ResponseEntity
                .ok(builder.data(programResponseDto).message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Retrieves a paginated list of programs with optional filtering by keyword.
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
     *            an optional keyword to filter programs by name or description
     * @return ResponseEntity containing a list of program details
     */
    @GetMapping
    @Operation(summary = "Get a list of programs", description = "Retrieves a paginated list of programs. Supports filtering by keyword in program name or description.")
    public ResponseEntity<AppResponseDto<List<ProgramResponseDto>>> getProgram(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<ProgramEntity> programPage = programService.getPrograms(pageNumber, pageSize, sortBy, direction, keyword);
        List<ProgramEntity> programs = programPage.getContent();
        return ResponseEntity.ok(
                AppResponseDto.<List<ProgramResponseDto>>builder().data(programService.toProgramResponseDto(programs))
                        .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                        .pageable(AppResponseDto.buildPageable(programPage, sortBy, direction)).build());
    }

    /**
     * Deletes a program by its ID.
     *
     * @param programId
     *            the ID of the program to be deleted
     * @return ResponseEntity indicating the result of the deletion
     */
    @DeleteMapping("/{programId}")
    @Operation(summary = "Delete a program", description = "Deletes a program identified by the programId. Returns a success message if deletion is successful.")
    public ResponseEntity<AppResponseDto<Void>> deleteProgram(@PathVariable UUID programId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        programService.deleteProgram(programId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the status of a program.
     *
     * @param programId
     *            the ID of the program to be updated
     * @param status
     *            the new status of the program
     * @return ResponseEntity indicating the result of the update
     */
    @PutMapping("/status/{programId}")
    @Operation(summary = "Update program status", description = "Updates the status of a program identified by the programId. Returns a success message if update is successful.")
    public ResponseEntity<AppResponseDto<Void>> updateProgramStatus(@PathVariable UUID programId,
            @RequestParam AppUtils.ProgramStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        programService.updateProgramStatus(programId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the flag of a program.
     *
     * @param programId
     *            the ID of the program to be updated
     * @param flag
     *            the new flag of the program
     * @return ResponseEntity indicating the result of the update
     */
    @PutMapping("/flag/{programId}")
    @Operation(summary = "Update program status", description = "Updates the status of a program identified by the programId. Returns a success message if update is successful.")
    public ResponseEntity<AppResponseDto<Void>> updateProgramStatus(@PathVariable UUID programId,
            @RequestParam AppUtils.ProgramFlag flag) {
        programService.updateProgramFlag(programId, flag);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Adds a new section to a program.
     *
     * @param sectionRequestDto
     *            the details of the section to be added
     * @return ResponseEntity containing the added section details
     */
    @PostMapping("/section")
    @Operation(summary = "Add a new section to a program", description = "Creates a new section within a program with the provided details. Returns the created section.")
    public ResponseEntity<AppResponseDto<SectionResponseDto>> addSection(
            @RequestBody @Valid SectionRequestDto sectionRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<SectionResponseDto> builder = AppResponseDto.builder();
        SectionEntity sectionEntity = sectionService.addSection(sectionRequestDto);
        SectionResponseDto sectionResponseDto = getSectionResponseDto(sectionEntity);
        return ResponseEntity
                .ok(builder.data(sectionResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing section.
     *
     * @param sectionId
     *            the ID of the section to be updated
     * @param sectionRequestDto
     *            the new details of the section
     * @return ResponseEntity containing the updated section details
     */
    @PutMapping("/section/{sectionId}")
    @Operation(summary = "Update an existing section", description = "Updates the details of an existing section identified by the sectionId. Returns the updated section.")
    public ResponseEntity<AppResponseDto<SectionResponseDto>> updateSection(@PathVariable UUID sectionId,
            @RequestBody @Valid SectionRequestDto sectionRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<SectionResponseDto> builder = AppResponseDto.builder();
        SectionEntity sectionEntity = sectionService.updateSection(sectionId, sectionRequestDto);
        SectionResponseDto sectionResponseDto = getSectionResponseDto(sectionEntity);
        return ResponseEntity
                .ok(builder.data(sectionResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a section by its ID.
     *
     * @param sectionId
     *            the ID of the section to be retrieved
     * @return ResponseEntity containing the section details
     */
    @GetMapping("/section/{sectionId}")
    @Operation(summary = "Get a section by ID", description = "Retrieves the details of a section identified by the sectionId. Returns the section details.")
    public ResponseEntity<AppResponseDto<SectionResponseDto>> getSection(@PathVariable UUID sectionId) {
        AppResponseDto.AppResponseDtoBuilder<SectionResponseDto> builder = AppResponseDto.builder();
        SectionEntity sectionEntity = sectionService.getSectionById(sectionId);
        SectionResponseDto sectionResponseDto = getSectionResponseDto(sectionEntity);
        return ResponseEntity
                .ok(builder.data(sectionResponseDto).message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Retrieves all sections associated with a specific program.
     *
     * @param programId
     *            the ID of the program for which sections are to be retrieved
     * @return ResponseEntity containing a list of section details
     */
    @GetMapping("/section/")
    @Operation(summary = "Get all sections by program ID", description = "Retrieves all sections associated with a specific program identified by the programId. Returns a list of sections.")
    public ResponseEntity<AppResponseDto<List<SectionResponseDto>>> getAllSectionsByProgramId(
            @RequestParam UUID programId) {
        List<SectionEntity> sections = sectionService.getAllSectionByProgramId(programId);
        return ResponseEntity.ok(
                AppResponseDto.<List<SectionResponseDto>>builder().data(sectionService.toSectionResponseDtos(sections))
                        .message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Deletes a section by its ID.
     *
     * @param sectionId
     *            the ID of the section to be deleted
     * @return ResponseEntity indicating the result of the deletion
     */
    @DeleteMapping("/section/{sectionId}")
    @Operation(summary = "Delete a section", description = "Deletes a section identified by the sectionId. Returns a success message if deletion is successful.")
    public ResponseEntity<AppResponseDto<Void>> deleteSection(@PathVariable UUID sectionId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        sectionService.deleteSection(sectionId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Maps a SectionEntity to a SectionResponseDto with additional details.
     *
     * @param sectionEntity
     *            the section audit to map
     * @return the mapped SectionResponseDto
     */
    private SectionResponseDto getSectionResponseDto(SectionEntity sectionEntity) {
        SectionResponseDto sectionResponseDto = AppUtils.map(sectionEntity, SectionResponseDto.class);
        sectionResponseDto.setCreatedByName(userService.getUserNameById(sectionEntity.getCreatedBy()));
        sectionResponseDto.setUpdatedByName(userService.getUserNameById(sectionEntity.getUpdatedBy()));
        return sectionResponseDto;
    }

    /**
     * Adds a new lesson to a section.
     *
     * @param lessonRequestDto
     *            the details of the lesson to be added
     * @return ResponseEntity containing the added lesson details
     */
    @PostMapping("/section/lesson")
    @Operation(summary = "Add a new lesson to a program", description = "Creates a new lesson within a lesson with the provided details. Returns the created lesson.")
    public ResponseEntity<AppResponseDto<LessonResponseDto>> addLesson(
            @RequestBody @Valid LessonRequestDto lessonRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<LessonResponseDto> builder = AppResponseDto.builder();
        LessonEntity lessonEntity = lessonService.addLesson(lessonRequestDto);
        LessonResponseDto lessonResponseDto = getLessonResponseDto(lessonEntity);
        return ResponseEntity
                .ok(builder.data(lessonResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing lesson.
     *
     * @param lessonId
     *            the ID of the lesson to be updated
     * @param lessonRequestDto
     *            the new details of the lesson
     * @return ResponseEntity containing the updated lesson details
     */
    @PutMapping("/section/lesson/{lessonId}")
    @Operation(summary = "Update an existing lesson", description = "Updates the details of an existing lesson identified by the lessonId. Returns the updated lesson.")
    public ResponseEntity<AppResponseDto<LessonResponseDto>> updateSection(@PathVariable UUID lessonId,
            @Valid @RequestBody LessonRequestDto lessonRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<LessonResponseDto> builder = AppResponseDto.builder();
        LessonEntity lessonEntity = lessonService.updateLesson(lessonId, lessonRequestDto);
        LessonResponseDto lessonResponseDto = getLessonResponseDto(lessonEntity);
        return ResponseEntity
                .ok(builder.data(lessonResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a lesson by its ID.
     *
     * @param lessonId
     *            the ID of the lesson to be retrieved
     * @return ResponseEntity containing the lesson details
     */
    @GetMapping("/section/lesson/{lessonId}")
    @Operation(summary = "Get a lesson by ID", description = "Retrieves the details of a lesson identified by the lessonId. Returns the lesson details.")
    public ResponseEntity<AppResponseDto<LessonResponseDto>> getLesson(@PathVariable UUID lessonId) {
        AppResponseDto.AppResponseDtoBuilder<LessonResponseDto> builder = AppResponseDto.builder();
        LessonEntity lessonEntity = lessonService.getLessonById(lessonId);
        LessonResponseDto lessonResponseDto = getLessonResponseDto(lessonEntity);
        return ResponseEntity
                .ok(builder.data(lessonResponseDto).message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Retrieves all lessons associated with a specific section.
     *
     * @param sectionId
     *            the ID of the section for which lessons are to be retrieved
     * @return ResponseEntity containing a list of lesson details
     */
    @GetMapping("/section/lesson")
    @Operation(summary = "Get all lessons by section ID", description = "Retrieves all lessons associated with a specific section identified by the sectionId. Returns a list of lessons.")
    public ResponseEntity<AppResponseDto<List<LessonResponseDto>>> getAllLessonsBySectionId(
            @RequestParam UUID sectionId) {
        List<LessonEntity> lessons = lessonService.getAllLessonByProgramId(sectionId);
        return ResponseEntity
                .ok(AppResponseDto.<List<LessonResponseDto>>builder().data(lessonService.toLessonResponseDtos(lessons))
                        .message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Deletes a lesson by its ID.
     *
     * @param lessonId
     *            the ID of the lesson to be deleted
     * @return ResponseEntity indicating the result of the deletion
     */
    @DeleteMapping("/section/lesson/{lessonId}")
    @Operation(summary = "Delete a lesson", description = "Deletes a lesson identified by the lessonId. Returns a success message if deletion is successful.")
    public ResponseEntity<AppResponseDto<Void>> deleteLesson(@PathVariable UUID lessonId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves the total number of programs in the system.
     *
     * @return ResponseEntity containing the total number of programs
     */
    @GetMapping("/dashboard/portal")
    public ResponseEntity<AppResponseDto<TotalDto>> getPortalDashboard() {
        AppResponseDto.AppResponseDtoBuilder<TotalDto> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(TotalDto.builder().total(programService.getTotalPrograms()).build())
                .message(AppUtils.Messages.PRACTICE_FOUND.getMessage()).build());
    }

    /**
     * Maps a LessonEntity to a LessonResponseDto with additional details.
     *
     * @param lessonEntity
     *            the lesson audit to map
     * @return the mapped LessonResponseDto
     */
    private LessonResponseDto getLessonResponseDto(LessonEntity lessonEntity) {
        LessonResponseDto lessonResponseDto = AppUtils.map(lessonEntity, LessonResponseDto.class);
        lessonResponseDto.setLessonStorageUrl(storageService.getStorageUrl(lessonEntity.getLessonStorageId()));
        lessonResponseDto.setCreatedByName(userService.getUserNameById(lessonEntity.getCreatedBy()));
        lessonResponseDto.setUpdatedByName(userService.getUserNameById(lessonEntity.getUpdatedBy()));
        return lessonResponseDto;
    }
}
