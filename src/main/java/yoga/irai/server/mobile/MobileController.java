package yoga.irai.server.mobile;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.dto.UserAoiDto;
import yoga.irai.server.authentication.dto.UserResponseDto;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.event.EventEntity;
import yoga.irai.server.event.EventService;
import yoga.irai.server.mobile.dto.*;
import yoga.irai.server.news.NewsEntity;
import yoga.irai.server.news.NewsService;
import yoga.irai.server.poem.PoemEntity;
import yoga.irai.server.poem.PoemService;
import yoga.irai.server.practice.*;
import yoga.irai.server.practice.category.PracticeCategoryService;
import yoga.irai.server.practice.user.PracticeUserEntity;
import yoga.irai.server.practice.user.PracticeUserRatingUpdateDto;
import yoga.irai.server.practice.user.PracticeUserRequestDto;
import yoga.irai.server.practice.user.PracticeUserResponseDto;
import yoga.irai.server.program.ProgramEntity;
import yoga.irai.server.program.ProgramService;
import yoga.irai.server.program.section.SectionEntity;
import yoga.irai.server.program.section.SectionService;
import yoga.irai.server.program.section.lesson.LessonEntity;
import yoga.irai.server.program.section.lesson.LessonService;
import yoga.irai.server.program.section.lesson.user.LessonUserEntity;
import yoga.irai.server.program.section.lesson.user.LessonUserRequestDto;
import yoga.irai.server.program.section.lesson.user.LessonUserResponseDto;
import yoga.irai.server.program.user.ProgramUserEntity;
import yoga.irai.server.program.user.ProgramUserRatingUpdateDto;
import yoga.irai.server.program.user.ProgramUserRequestDto;
import yoga.irai.server.program.user.ProgramUserResponseDto;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.shorts.ShortsEntity;
import yoga.irai.server.shorts.ShortsService;
import yoga.irai.server.storage.StorageService;

/**
 * MobileController provides APIs for mobile applications to access various
 * resources such as practices, poems, shorts, events, and user information.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/mobile")
public class MobileController {

    private final NewsService newsService;
    private final UserService userService;
    private final PoemService poemService;
    private final EventService eventService;
    private final LessonService lessonService;
    private final ShortsService shortsService;
    private final ProgramService programService;
    private final SectionService sectionService;
    private final SettingService settingService;
    private final StorageService storageService;
    private final PracticeService practiceService;
    private final PracticeCategoryService practiceCategoryService;

    /**
     * Get a paginated list of practice categories for dropdown selection.
     *
     * @return ResponseEntity containing a list of PracticeCategoryListResponseDto
     */
    @Tag(name = "Practices", description = "Endpoints for managing and retrieving practice-related data.")
    @GetMapping("/practice/category")
    @Operation(summary = "Get Practice Category Dropdown", description = "Get a paginated list of practice categories "
            + "for dropdown selection based on a keyword. Returns a paginated list of practice categories matching the keyword.")
    public ResponseEntity<AppResponseDto<List<PracticeCategoryListResponseDto>>> getPracticeCategoryList() {
        AppResponseDto.AppResponseDtoBuilder<List<PracticeCategoryListResponseDto>> builder = AppResponseDto.builder();
        List<PracticeCategoryListResponseDto> dropdownResponseDtos = practiceCategoryService.getPracticeCategoryList()
                .stream().map(dto -> {
                    PracticeCategoryListResponseDto dropdownResponseDto = AppUtils.map(dto,
                            PracticeCategoryListResponseDto.class);
                    if (!ObjectUtils.isEmpty(dto.getPracticeCategoryIconStorageId())) {
                        dropdownResponseDto.setPracticeCategoryIconStorageUrl(
                                storageService.getStorageUrl(dto.getPracticeCategoryIconStorageId()));
                    } else {
                        dropdownResponseDto.setPracticeCategoryIconStorageUrl(dto.getPracticeCategoryIconExternalUrl());
                    }
                    return dropdownResponseDto;
                }).toList();
        return ResponseEntity
                .ok(builder.data(dropdownResponseDtos).message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Get a paginated list of practices based on various filters like keyword and
     * category ID.
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
     *            an optional keyword to filter practices
     * @param categoryId
     *            an optional category ID to filter practices
     * @return ResponseEntity containing a paginated list of
     *         PracticeMobileResponseDto
     */
    @Tag(name = "Practices")
    @GetMapping("/practice")
    @Operation(summary = "Get Practices", description = "Get a paginated list of practices based on various filters "
            + "like keyword and category ID. Returns a paginated list of practices.")
    public ResponseEntity<AppResponseDto<List<PracticeMobileResponseDto>>> getPractices(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) UUID categoryId) {
        Page<PracticeEntity> practicePage = practiceService.getPractices(pageNumber, pageSize, sortBy, direction,
                keyword, categoryId);
        List<PracticeEntity> practiceEntities = practicePage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<PracticeMobileResponseDto>>builder()
                .data(practiceService.toPracticeMobileResponseDto(practiceEntities))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.Pageable.builder().pageNumber(pageNumber).pageSize(pageSize)
                        .totalPages(practicePage.getTotalPages()).totalElements(practicePage.getTotalElements())
                        .sortBy(sortBy).sortDirection(direction.name()).build())
                .build());
    }

    /**
     * Get a paginated list of poems based on various filters like keyword.
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
     *            an optional keyword to filter poems
     * @return ResponseEntity containing a paginated list of PoemMobileResponseDto
     */
    @Tag(name = "Poems", description = "Endpoints for managing and retrieving poem-related data.")
    @GetMapping("/poems")
    @Operation(summary = "Get Poems", description = "Get a paginated list of poems based on various filters "
            + "like keyword and category ID. Returns a paginated list of poems.")
    public ResponseEntity<AppResponseDto<List<PoemMobileResponseDto>>> getPoems(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<PoemEntity> poemPage = poemService.getPoems(pageNumber, pageSize, sortBy, direction, keyword);
        List<PoemEntity> poems = poemPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<PoemMobileResponseDto>>builder()
                .data(poemService.toPoemMobileResponseDto(poems)).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(poemPage, sortBy, direction)).build());
    }

    /**
     * Get a paginated list of shorts based on various filters like keyword.
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
     *            an optional keyword to filter shorts
     * @return ResponseEntity containing a paginated list of ShortsMobileResponseDto
     */
    @Tag(name = "Shorts", description = "Endpoints for managing and retrieving shorts-related data.")
    @GetMapping("/shorts")
    @Operation(summary = "Get Shorts", description = "Get a paginated list of shorts based on various filters "
            + "like keyword. Returns a paginated list of shorts.")
    public ResponseEntity<AppResponseDto<List<ShortsMobileResponseDto>>> getShorts(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<ShortsEntity> shortsPage = shortsService.getShorts(pageNumber, pageSize, sortBy, direction, keyword);
        List<ShortsEntity> shortsEntities = shortsPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<ShortsMobileResponseDto>>builder()
                .data(shortsService.toShortsMobileResponseDto(shortsEntities))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(shortsPage, sortBy, direction)).build());
    }

    /**
     * Get a paginated list of events based on a keyword.
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
     *            an optional keyword to filter events
     * @return ResponseEntity containing a paginated list of EventMobileResponseDto
     */
    @Tag(name = "Events", description = "Endpoints for managing and retrieving event-related data.")
    @GetMapping("/events")
    @Operation(summary = "Get Events", description = "Get events based on a keyword. Returns a paginated list of events matching the keyword.")
    public ResponseEntity<AppResponseDto<List<EventMobileResponseDto>>> getEvents(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<EventEntity> eventPage = eventService.getEvents(pageNumber, pageSize, sortBy, direction, keyword);
        List<EventEntity> eventEntities = eventPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<EventMobileResponseDto>>builder()
                .data(eventService.toEventMobileResponseDto(eventEntities))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(eventPage, sortBy, direction)).build());
    }

    /**
     * Get a paginated list of news articles based on various filters like
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
     *            an optional keyword to filter events
     * @return ResponseEntity containing a paginated list of NewsResponseDto
     */
    @Tag(name = "News", description = "Endpoints for managing and retrieving news-related data.")
    @GetMapping("/news")
    @Operation(summary = "Get News", description = "Get a paginated list of news articles based on various filters "
            + "like keyword. Returns a paginated list of news articles.")
    public ResponseEntity<AppResponseDto<List<NewsMobileResponseDto>>> getNewsList(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<NewsEntity> newsPage = newsService.getNews(pageNumber, pageSize, sortBy, direction, keyword);
        List<NewsEntity> newsEntities = newsPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<NewsMobileResponseDto>>builder()
                .data(newsService.toNewsMobileResponseDto(newsEntities))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(newsPage, sortBy, direction)).build());
    }

    /**
     * Updates the view count of a news article.
     *
     * @param newsId
     *            the ID of the news article to update
     * @return ResponseEntity containing the response data
     */
    @Tag(name = "News")
    @PutMapping("/news/view/{newsId}")
    @Operation(summary = "Update news view count", description = "This endpoint allows users to update the view count of a news article.")
    public ResponseEntity<AppResponseDto<Void>> updateNewsViewCount(@PathVariable UUID newsId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        newsService.updateViewCount(newsId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the like count of a news article.
     *
     * @param newsId
     *            the ID of the news article to update
     * @return ResponseEntity containing the response data
     */
    @Tag(name = "News")
    @PutMapping("/news/like/{newsId}")
    @Operation(summary = "Update news like count", description = "This endpoint allows users to update the like count of a news article.")
    public ResponseEntity<AppResponseDto<Void>> updateNewsLikeCount(@PathVariable UUID newsId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        newsService.updateLikeCount(newsId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Get a paginated list of programs based on various filters like keyword.
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
     *            an optional keyword to filter programs
     * @return ResponseEntity containing a paginated list of ProgramResponseDto
     */
    @Tag(name = "Programs", description = "Endpoints for managing and retrieving program-related data.")
    @GetMapping("/program")
    @Operation(summary = "Get a list of programs", description = "Retrieves a paginated list of programs. Supports filtering by keyword in program name or description.")
    public ResponseEntity<AppResponseDto<List<ProgramMobileResponseDto>>> getProgram(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<ProgramEntity> programPage = programService.getPrograms(pageNumber, pageSize, sortBy, direction, keyword);
        List<ProgramEntity> programs = programPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<ProgramMobileResponseDto>>builder()
                .data(programService.toProgramMobileResponseDto(programs))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(programPage, sortBy, direction)).build());
    }

    /**
     * Get all sections by program ID.
     *
     * @param programId
     *            the ID of the program to retrieve sections for
     * @return ResponseEntity containing a list of SectionResponseDto
     */
    @Tag(name = "Programs")
    @GetMapping("/program/section/{programId}")
    @Operation(summary = "Get all sections by program ID", description = "Retrieves all sections associated with a specific program identified by the programId. Returns a list of sections.")
    public ResponseEntity<AppResponseDto<List<SectionMobileResponseDto>>> getSectionsByProgramId(
            @PathVariable UUID programId) {
        List<SectionEntity> sections = sectionService.getAllSectionByProgramId(programId);
        return ResponseEntity.ok(AppResponseDto.<List<SectionMobileResponseDto>>builder()
                .data(sectionService.toSectionMobileResponseDtos(sections))
                .message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Get all lessons by section ID.
     *
     * @param sectionId
     *            the ID of the section to retrieve lessons for
     * @return ResponseEntity containing a list of LessonResponseDto
     */
    @Tag(name = "Programs")
    @GetMapping("/program/section/lesson/{sectionId}")
    @Operation(summary = "Get all lessons by section ID", description = "Retrieves all lessons associated with a specific section identified by the sectionId. Returns a list of lessons.")
    public ResponseEntity<AppResponseDto<List<LessonMobileResponseDto>>> getAllLessonsBySectionId(
            @PathVariable UUID sectionId) {
        List<LessonEntity> lessons = lessonService.getAllLessonByProgramId(sectionId);
        return ResponseEntity.ok(AppResponseDto.<List<LessonMobileResponseDto>>builder()
                .data(lessonService.toLessonMobileResponseDtos(lessons))
                .message(AppUtils.Messages.SECTION_FOUND.getMessage()).build());
    }

    /**
     * Get the principal user information based on the logged-in user's ID.
     *
     * @return ResponseEntity containing UserResponseDto with user details
     */
    @Tag(name = "User", description = "Endpoints for managing and retrieving user-related data.")
    @GetMapping("/user")
    @Operation(summary = "Get Principal User", description = "Get principal user based on the Login")
    public ResponseEntity<AppResponseDto<UserResponseDto>> getPrincipalUser() {
        AppResponseDto.AppResponseDtoBuilder<UserResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService.getUserById(AppUtils.getPrincipalUserId());
        UserResponseDto userResponseDto = userService.getUserDto(userEntity);
        return ResponseEntity
                .ok(builder.data(userResponseDto).message(AppUtils.Messages.USER_FOUND.getMessage()).build());
    }

    /**
     * Get the list of User AOI questions.
     *
     * @return ResponseEntity containing a list of UserAoiDto with questions and
     *         options
     */
    @Tag(name = "User")
    @GetMapping("/aoi/questions")
    @Operation(summary = "Get User AOI Questions", description = "Get the list of"
            + "User AOI questions. Returns a list of questions with options.")
    public ResponseEntity<AppResponseDto<List<UserAoiDto>>> getUserAOIQuestions() {
        AppResponseDto.AppResponseDtoBuilder<List<UserAoiDto>> builder = AppResponseDto.builder();
        List<UserAoiDto> userAoiDtos = AppUtils.readValue(settingService
                .getSettingBySettingName(AppUtils.SettingName.USER_AOI_QUESTIONS.getSetting()).getSettingValue(),
                new TypeReference<>() {
                });
        return ResponseEntity
                .ok(builder.data(userAoiDtos).message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Updates the user's Area of Interest (AOI) answers.
     *
     * @param answers
     *            a JSON string representing the user's AOI answers
     * @return ResponseEntity containing the response data
     */
    @Tag(name = "User")
    @PutMapping("/aoi")
    public ResponseEntity<AppResponseDto<Void>> updateUserAOIQuestions(@RequestParam String answers) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.updateUserAoiAnswers(answers);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Updates the view count of a poem.
     *
     * @param poemId
     *            the ID of the poem to update
     * @return ResponseEntity containing the response data
     */
    @Tag(name = "Poems")
    @PutMapping("/poem/view/{poemId}")
    @Operation(summary = "Update poem view count", description = "This endpoint allows users to update the view count of a poem.")
    public ResponseEntity<AppResponseDto<Void>> changePoemStatus(@PathVariable UUID poemId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        poemService.updateViewCount(poemId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Creates a new practice user.
     *
     * @param practiceUserRequestDto
     *            - contains all the data required to create a practice user.
     * @return ResponseEntity with PracticeUserResponseDto containing the newly
     *         created practice user
     */
    @Tag(name = "Practices")
    @PostMapping("/practice/user")
    @Operation(summary = "Update Practice User", description = "Update an existing practice user by their ID")
    public ResponseEntity<AppResponseDto<PracticeUserResponseDto>> updatePracticeUser(
            @Valid @RequestBody PracticeUserRequestDto practiceUserRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PracticeUserResponseDto> builder = AppResponseDto.builder();
        PracticeUserEntity practiceUserEntity = practiceService.updatePracticeUser(practiceUserRequestDto);
        PracticeUserResponseDto practiceUserResponseDto = getPracticeUserResponseDto(practiceUserEntity);
        return ResponseEntity.ok(
                builder.data(practiceUserResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing practice user rating and comment.
     *
     * @param practiceUserId
     *            the ID of the practice user to update
     * @param practiceUserRatingUpdateDto
     *            contains the new rating and comment for the practice user
     * @return ResponseEntity with a success message
     */
    @Tag(name = "Practices")
    @PutMapping("/practice/user/rating/{practiceUserId}")
    @Operation(summary = "Update Practice User Rating", description = "Update an existing practice user rating by their ID")
    public ResponseEntity<AppResponseDto<Void>> updatePracticeUserRating(@PathVariable UUID practiceUserId,
            @Valid @RequestBody PracticeUserRatingUpdateDto practiceUserRatingUpdateDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceService.changeRatingAndComment(practiceUserId, practiceUserRatingUpdateDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a practice user by their ID.
     *
     * @param practiceUserId
     *            - the ID of the practice user to retrieve
     * @return ResponseEntity with PracticeUserResponseDto containing the practice
     *         user details
     */
    @Tag(name = "Practices")
    @PutMapping("practice/user/status/{practiceUserId}")
    @Operation(summary = "Update Practice User status", description = "Update an existing practice user status by their ID")
    public ResponseEntity<AppResponseDto<Void>> updatePracticeUserStatus(@PathVariable UUID practiceUserId,
            @RequestParam AppUtils.PracticeUserStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceService.changePracticeUserStatus(practiceUserId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the view count of a short's entry.
     *
     * @param shortsId
     *            the ID of the shorts to update view count
     * @return a response audit indicating the result of the view count update
     */
    @Tag(name = "Shorts")
    @PutMapping("/shorts/view/{shortsId}")
    @Operation(summary = "Update shorts view count", description = "Increments the view count of a shorts entry.")
    public ResponseEntity<AppResponseDto<Void>> changeViewCount(@PathVariable UUID shortsId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        shortsService.updateViewCount(shortsId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the like count of a shorts' entry.
     *
     * @param shortsId
     *            the ID of the shorts to update like count
     * @return a response audit indicating the result of the like count update
     */
    @Tag(name = "Shorts")
    @PutMapping("/shorts/like/{shortsId}")
    @Operation(summary = "Update shorts like count", description = "Increments the like count of a shorts entry.")
    public ResponseEntity<AppResponseDto<Void>> changeLikeCount(@PathVariable UUID shortsId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        shortsService.updateLikeCount(shortsId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Creates a new program user.
     *
     * @param programUserRequestDto
     *            - contains all the data required to create a practice user.
     * @return ResponseEntity with {@link ProgramUserResponseDto} containing the
     *         newly created practice user
     */
    @Tag(name = "Programs")
    @PostMapping("/program/user")
    @Operation(summary = "Update Program User", description = "Update an existing program user by their ID")
    public ResponseEntity<AppResponseDto<ProgramUserResponseDto>> updateProgramUser(
            @Valid @RequestBody ProgramUserRequestDto programUserRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<ProgramUserResponseDto> builder = AppResponseDto.builder();
        ProgramUserEntity programUserEntity = programService.updateProgramUser(programUserRequestDto);
        ProgramUserResponseDto programUserResponseDto = getProgramUserResponseDto(programUserEntity);
        return ResponseEntity.ok(
                builder.data(programUserResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing program user rating and comment.
     *
     * @param programUserId
     *            the ID of the practice user to update
     * @param practiceUserRatingUpdateDto
     *            contains the new rating and comment for the practice user
     * @return ResponseEntity with a success message
     */
    @Tag(name = "Programs")
    @PutMapping("/program/user/rating/{programUserId}")
    @Operation(summary = "Update Program User Rating", description = "Update an existing program user rating by their ID")
    public ResponseEntity<AppResponseDto<Void>> updateProgramUserRating(@PathVariable UUID programUserId,
            @Valid @RequestBody ProgramUserRatingUpdateDto practiceUserRatingUpdateDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        programService.changeRatingAndComment(programUserId, practiceUserRatingUpdateDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a program user by their ID.
     *
     * @param programUserId
     *            - the ID of the program user to retrieve
     * @return ResponseEntity with ProgramUserResponseDto containing the practice
     *         user details
     */
    @Tag(name = "Programs")
    @PutMapping("/program/user/status/{programUserId}")
    @Operation(summary = "Update Program User status", description = "Update an existing program user status by their ID")
    public ResponseEntity<AppResponseDto<Void>> updateProgramUserStatus(@PathVariable UUID programUserId,
            @RequestParam AppUtils.ProgramUserStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        programService.changeProgramUserStatus(programUserId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Creates a new lesson user.
     *
     * @param lessonUserRequestDto
     *            - contains all the data required to create a lesson user.
     * @return ResponseEntity with {@link ProgramUserResponseDto} containing the
     *         newly created lesson user
     */
    @Tag(name = "Programs")
    @PostMapping("/program/section/lesson/user")
    @Operation(summary = "Update Program User", description = "Update an existing program user by their ID")
    public ResponseEntity<AppResponseDto<LessonUserResponseDto>> updateLessonUser(
            @Valid @RequestBody LessonUserRequestDto lessonUserRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<LessonUserResponseDto> builder = AppResponseDto.builder();
        LessonUserEntity lessonUserEntity = lessonService.updateLessonUser(lessonUserRequestDto);
        LessonUserResponseDto lessonUserResponseDto = getLessonUserResponseDto(lessonUserEntity);
        return ResponseEntity
                .ok(builder.data(lessonUserResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a lesson user by their ID.
     *
     * @param lessonUserId
     *            - the ID of the Lesson user to retrieve
     * @return ResponseEntity with LessonUserResponseDto containing the practice
     *         user details
     */
    @Tag(name = "Programs")
    @PutMapping("/program/section/lesson/user/status/{lessonUserId}")
    @Operation(summary = "Update Lesson User status", description = "Update an existing lesson user status by their ID")
    public ResponseEntity<AppResponseDto<Void>> updateLessonUserStatus(@PathVariable UUID lessonUserId,
            @RequestParam AppUtils.LessonUserStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        lessonService.changeLessonUserStatus(lessonUserId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a practice user response DTO based on the provided practice user
     * audit.
     *
     * @param practiceUserEntity
     *            the PracticeUserEntity to convert to a response DTO
     * @return PracticeUserResponseDto containing the details of the practice user
     */
    private PracticeUserResponseDto getPracticeUserResponseDto(PracticeUserEntity practiceUserEntity) {
        PracticeUserResponseDto practiceUserResponseDto = AppUtils.map(practiceUserEntity,
                PracticeUserResponseDto.class);
        practiceUserResponseDto.setPracticeTitle(
                practiceService.getPracticeById(practiceUserEntity.getPracticeId()).getPracticeName());
        practiceUserResponseDto.setUserName(userService.getUserNameById(practiceUserEntity.getUserId()));
        return practiceUserResponseDto;
    }

    /**
     * Retrieves a program user response DTO based on the provided program user
     * audit.
     *
     * @param programUserEntity
     *            the ProgramUserEntity to convert to a response DTO
     * @return ProgramUserResponseDto containing the details of the program user
     */
    private ProgramUserResponseDto getProgramUserResponseDto(ProgramUserEntity programUserEntity) {
        ProgramUserResponseDto programUserResponseDto = AppUtils.map(programUserEntity, ProgramUserResponseDto.class);
        programUserResponseDto
                .setProgramName(programService.getProgramById(programUserEntity.getProgramId()).getProgramName());
        programUserResponseDto.setUserName(userService.getUserNameById(programUserEntity.getUserId()));
        return programUserResponseDto;
    }

    /**
     * Retrieves a lesson user response DTO based on the provided lesson user audit.
     *
     * @param lessonUserEntity
     *            the LessonUserEntity to convert to a response DTO
     * @return LessonUserResponseDto containing the details of the lesson user
     */
    private LessonUserResponseDto getLessonUserResponseDto(LessonUserEntity lessonUserEntity) {
        LessonUserResponseDto lessonUserResponseDto = AppUtils.map(lessonUserEntity, LessonUserResponseDto.class);
        lessonUserResponseDto
                .setLessonName(lessonService.getLessonById(lessonUserEntity.getLessonId()).getLessonName());
        lessonUserResponseDto.setUserName(userService.getUserNameById(lessonUserEntity.getUserId()));
        return lessonUserResponseDto;
    }

    /**
     * Retrieves the top 3 items for the mobile dashboard. This includes poems,
     * practices, programs, shorts, and events.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard", description = "Endpoints for retrieving dashboard-related data.")
    @GetMapping("/dashboard/poems")
    @Operation(summary = "Get Poem Dashboard", description = "Get the top 3 poems for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<PoemMobileResponseDto>>> getPoemDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<PoemMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getPoemsForMobileDashboard()).build());
    }

    /**
     * Retrieves the top 3 practices, programs, shorts, and events for the mobile
     * dashboard.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard")
    @GetMapping("/dashboard/practice")
    @Operation(summary = "Get Practice Dashboard", description = "Get the top 3 practices for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<PracticeMobileResponseDto>>> getPracticeDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<PracticeMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getPracticesForMobileDashboard()).build());
    }

    /**
     * Retrieves the top 3 programs, shorts, and events for the mobile dashboard.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard")
    @GetMapping("/dashboard/program")
    @Operation(summary = "Get Program Dashboard", description = "Get the top 3 programs for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<ProgramMobileResponseDto>>> getProgramDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<ProgramMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getProgramsForMobileDashBoard()).build());
    }

    /**
     * * Retrieves the top 3 shorts and events for the mobile dashboard.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard")
    @GetMapping("/dashboard/shorts")
    @Operation(summary = "Get Shorts Dashboard", description = "Get the top 3 shorts for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<ShortsMobileResponseDto>>> getShortsDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<ShortsMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getShortsForMobileDashboard()).build());
    }

    /**
     * Retrieves the top 3 events for the mobile dashboard.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard")
    @GetMapping("/dashboard/events")
    @Operation(summary = "Get Event Dashboard", description = "Get the top 3 events for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<EventMobileResponseDto>>> getEventDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<EventMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getEventsForMobileDashboard()).build());
    }

    /**
     * Retrieves the top 3 news for the mobile dashboard.
     *
     * @return ResponseEntity containing the dashboard data
     */
    @Tag(name = "Dashboard")
    @GetMapping("/dashboard/news")
    @Operation(summary = "Get News Dashboard", description = "Get the top 3 News for the mobile dashboard.")
    public ResponseEntity<AppResponseDto<List<NewsMobileResponseDto>>> getNewsDashboard() {
        AppResponseDto.AppResponseDtoBuilder<List<NewsMobileResponseDto>> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(getNewsForMobileDashboard()).build());
    }

    /**
     * Retrieves the top 3 poems for the mobile dashboard.
     *
     * @return a list of PoemResponseDto containing the top 3 poems
     */
    private List<PoemMobileResponseDto> getPoemsForMobileDashboard() {
        return poemService.getTop3Poems().stream()
                .map(poemEntity -> PoemMobileResponseDto.builder().poemId(poemEntity.getPoemId())
                        .poemName(poemEntity.getPoemName()).poemIconExternalUrl(poemEntity.getPoemIconExternalUrl())
                        .poemIconStorageUrl(storageService.getStorageUrl(poemEntity.getPoemStorageId()))
                        .poemAuthor(poemEntity.getPoemAuthor())
                        .poemBannerExternalUrl(poemEntity.getPoemBannerExternalUrl())
                        .poemBannerStorageUrl(storageService.getStorageUrl(poemEntity.getPoemBannerStorageId()))
                        .poemDescription(poemEntity.getPoemDescription()).build())
                .toList();
    }

    /**
     * Retrieves the top 3 practices for the mobile dashboard.
     *
     * @return a list of PracticeResponseDto containing the top 3 practices
     */
    private List<PracticeMobileResponseDto> getPracticesForMobileDashboard() {
        return practiceService.getTop3Practices().stream()
                .map(practiceEntity -> PracticeMobileResponseDto.builder().practiceId(practiceEntity.getPracticeId())
                        .practiceName(practiceEntity.getPracticeName())
                        .practiceIconExternalUrl(practiceEntity.getPracticeIconExternalUrl())
                        .practiceIconStorageUrl(storageService.getStorageUrl(practiceEntity.getPracticeIconStorageId()))
                        .practiceDescription(practiceEntity.getPracticeDescription()).build())
                .toList();
    }

    /**
     * Retrieves the top 3 programs for the mobile dashboard.
     *
     * @return a list of ProgramResponseDto containing the top 3 programs
     */
    private List<ProgramMobileResponseDto> getProgramsForMobileDashBoard() {
        return programService.getTop3Programs().stream().map(programEntity -> ProgramMobileResponseDto.builder()
                .programId(programEntity.getProgramId()).programName(programEntity.getProgramName())
                .programBannerExternalUrl(programEntity.getProgramBannerExternalUrl())
                .programBannerStorageUrl(storageService.getStorageUrl(programEntity.getProgramBannerStorageId()))
                .programDescription(programEntity.getProgramDescription()).build()).toList();
    }

    /**
     * Retrieves the top 3 shorts for the mobile dashboard.
     *
     * @return a list of ShortsResponseDto containing the top 3 shorts
     */
    private List<ShortsMobileResponseDto> getShortsForMobileDashboard() {
        return shortsService.getTop3Shorts().stream()
                .map(shortsEntity -> ShortsMobileResponseDto.builder().shortsId(shortsEntity.getShortsId())
                        .shortsName(shortsEntity.getShortsName())
                        .shortsBannerExternalUrl(shortsEntity.getShortsBannerExternalUrl())
                        .shortsBannerStorageUrl(storageService.getStorageUrl(shortsEntity.getShortsBannerStorageId()))
                        .shortsDescription(shortsEntity.getShortsDescription()).build())
                .toList();
    }

    /**
     * Retrieves the top 3 events for the mobile dashboard.
     *
     * @return a list of EventMobileResponseDto containing the top 3 events
     */
    private List<EventMobileResponseDto> getEventsForMobileDashboard() {
        return eventService.getTop3Events().stream()
                .map(event -> EventMobileResponseDto.builder().eventId(event.getEventId())
                        .eventName(event.getEventName()).eventIconExternalUrl(event.getEventIconExternalUrl())
                        .eventIconStorageUrl(storageService.getStorageUrl(event.getEventIconStorageId()))
                        .eventStartDateTime(event.getEventStartDateTime()).eventEndDateTime(event.getEventEndDateTime())
                        .eventDescription(event.getEventDescription()).addresses(event.getAddresses()).build())
                .toList();
    }

    /**
     * Retrieves the top 3 news for the mobile dashboard.
     *
     * @return a list of NewsMobileResponseDto containing the top 3 news
     */
    private List<NewsMobileResponseDto> getNewsForMobileDashboard() {
        return newsService.getTop3News().stream()
                .map(news -> NewsMobileResponseDto.builder().newsId(news.getNewsId()).newsName(news.getNewsName())
                        .newsIconExternalUrl(news.getNewsIconExternalUrl())
                        .newsIconStorageUrl(storageService.getStorageUrl(news.getNewsIconStorageId()))
                        .newsBannerExternalUrl(news.getNewsBannerExternalUrl())
                        .newsBannerStorageUrl(storageService.getStorageUrl(news.getNewsBannerStorageId()))
                        .newsDescription(news.getNewsDescription()).isRecommended(news.getIsRecommended())
                        .likes(news.getLikes()).views(news.getViews()).build())
                .toList();
    }
}
