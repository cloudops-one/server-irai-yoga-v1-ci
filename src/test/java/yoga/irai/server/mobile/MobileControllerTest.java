package yoga.irai.server.mobile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;
import yoga.irai.server.authentication.dto.UserAoiDto;
import yoga.irai.server.authentication.dto.UserAoiOptionDto;
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
import yoga.irai.server.practice.PracticeEntity;
import yoga.irai.server.practice.PracticeService;
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
import yoga.irai.server.setting.SettingEntity;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.shorts.ShortsEntity;
import yoga.irai.server.shorts.ShortsService;
import yoga.irai.server.storage.StorageService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileControllerTest {
    @Mock
    private NewsService newsService;

    @Mock
    private UserService userService;

    @Mock
    private PoemService poemService;

    @Mock
    private EventService eventService;

    @Mock
    private LessonService lessonService;

    @Mock
    private ShortsService shortsService;

    @Mock
    private ProgramService programService;

    @Mock
    private SectionService sectionService;

    @Mock
    private SettingService settingService;

    @Mock
    private StorageService storageService;

    @Mock
    private PracticeService practiceService;

    @Mock
    private PracticeCategoryService practiceCategoryService;

    @InjectMocks
    private MobileController mobileController;

    private UserEntity userEntity;
    private NewsEntity newsEntity;
    private PoemEntity poemEntity;
    private EventEntity eventEntity;
    private ShortsEntity shortsEntity;
    private LessonEntity lessonEntity;
    private ProgramEntity programEntity;
    private SectionEntity sectionEntity;
    private SettingEntity settingEntity;
    private PracticeEntity practiceEntity;
    private LessonUserEntity lessonUserEntity;
    private ProgramUserEntity programUserEntity;
    private PracticeUserEntity practiceUserEntity;

    private UserAoiDto userAoiDto;
    private UserResponseDto userResponseDto;
    private ProgramUserRequestDto programUserRequestDto;
    private NewsMobileResponseDto newsMobileResponseDto;
    private PoemMobileResponseDto poemMobileResponseDto;
    private EventMobileResponseDto eventMobileResponseDto;
    private ShortsMobileResponseDto shortsMobileResponseDto;
    private LessonMobileResponseDto lessonMobileResponseDto;
    private SectionMobileResponseDto sectionMobileResponseDto;
    private ProgramMobileResponseDto programMobileResponseDto;
    private PracticeMobileResponseDto practiceMobileResponseDto;

    private Page<NewsEntity> newsPage;
    private Page<PoemEntity> poemPage;
    private Page<EventEntity> eventsPage;
    private Page<ShortsEntity> shortsPage;
    private Page<ProgramEntity> programPage;
    private Page<PracticeEntity> practicePage;

    private LessonUserRequestDto lessonUserRequestDto;
    private PracticeUserRequestDto practiceUserRequestDto;
    private ProgramUserRatingUpdateDto programUserRatingUpdateDto;
    private PracticeUserRatingUpdateDto practiceUserRatingUpdateDto;

    @BeforeEach
    void setUp() {
        practiceMobileResponseDto = PracticeMobileResponseDto.builder()
                .practiceName("practiceName").practiceDescription("practiceDescription")
                .practiceCategoryName("practiceCategoryName").rating(5F)
                .tags(Set.of("tag1", "tag2")).duration(1000L).practiceId(UUID.randomUUID())
                .practiceUserStatus(AppUtils.PracticeUserStatus.IN_PROGRESS)
                .ratingCount(5L).practiceIconExternalUrl("practiceIconExternalUrl")
                .practiceBannerExternalUrl("practiceBannerExternalUrl")
                .practiceExternalUrl("practiceExternalUrl").build();
        practiceEntity = PracticeEntity.builder()
                .practiceName("practiceName").practiceDescription("practiceDescription")
                .rating(5F).tags("[\"tag1\", \"tag2\"]").duration(1000L).practiceId(UUID.randomUUID())
                .ratingCount(5L).practiceIconExternalUrl("practiceIconExternalUrl")
                .practiceBannerExternalUrl("practiceBannerExternalUrl")
                .practiceExternalUrl("practiceExternalUrl")
                .practiceBannerStorageId(UUID.randomUUID())
                .practiceStorageId(UUID.randomUUID())
                .practiceIconStorageId(UUID.randomUUID())
                .build();
        practicePage = new PageImpl<>(List.of(practiceEntity));
        poemMobileResponseDto = PoemMobileResponseDto.builder()
                .poemName("poemName").poemDescription("poemDescription").poemAuthor("poemAuthor")
                .poemDuration(1000L).poemTags(Set.of("tag1", "tag2"))
                .poemId(UUID.randomUUID()).poemIconExternalUrl("poemIconExternalUrl")
                .poemStorageUrl("poemStorageUrl").poemBannerExternalUrl("poemBannerExternalUrl")
                .poemText("poemText").poemViews(100L).build();
        poemEntity = PoemEntity.builder().poemId(UUID.randomUUID()).poemName("poemName")
                .poemDescription("poemDescription").poemAuthor("poemAuthor")
                .poemStatus(AppUtils.PoemStatus.ACTIVE)
                .poemDuration(1000L).poemTags("[\"tag1\", \"tag2\"]")
                .poemIconExternalUrl("poemIconExternalUrl").poemExternalUrl("poemStorageUrl")
                .poemBannerExternalUrl("poemBannerExternalUrl")
                .poemStorageId(UUID.randomUUID())
                .poemText("poemText").poemViews(100L).build();
        poemPage = new PageImpl<>(List.of(poemEntity));
        shortsMobileResponseDto = ShortsMobileResponseDto.builder()
                .shortsName("shortsName").shortsDescription("shortsDescription").shortsId(UUID.randomUUID())
                .duration(1000L).likes(100L).orgName("orgName").tags(Set.of("tag1", "tag2"))
                .shortsBannerExternalUrl("shortsBannerExternalUrl")
                .shortsExternalUrl("shortsExternalUrl").build();
        shortsEntity = ShortsEntity.builder().shortsName("shortsName")
                .shortsDescription("shortsDescription").shortsId(UUID.randomUUID())
                .duration(1000L).likes(100L).orgId(UUID.randomUUID()).tags("[\"tag1\", \"tag2\"]")
                .shortsBannerExternalUrl("shortsBannerExternalUrl")
                .shortsBannerStorageId(UUID.randomUUID())
                .shortsExternalUrl("shortsExternalUrl").build();
        shortsPage = new PageImpl<>(List.of(shortsEntity));
        eventMobileResponseDto = EventMobileResponseDto.builder()
                .eventName("eventName").eventDescription("eventDescription").eventId(UUID.randomUUID())
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build())).urls(List.of(UrlDto.builder().id(0).url("http://test.test")
                        .type(AppUtils.UrlType.REGISTRATION_LINK).build()))
                .build();
        eventEntity = EventEntity.builder().eventId(UUID.randomUUID()).eventName("eventName")
                .eventDescription("eventDescription").eventId(UUID.randomUUID())
                .eventIconStorageId(UUID.randomUUID())
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build())).urls(List.of(UrlDto.builder().id(0).url("http://test.test")
                        .type(AppUtils.UrlType.REGISTRATION_LINK).build())).build();
        eventsPage = new PageImpl<>(List.of(eventEntity));
        newsMobileResponseDto = NewsMobileResponseDto.builder()
                .newsName("newsName").newsDescription("newsDescription").newsId(UUID.randomUUID())
                .newsIconExternalUrl("newsIconStorageUrl").newsBannerExternalUrl("newsIconExternalUrl")
                .likes(100L).isRecommended(true).tags(Set.of("tag1", "tag2"))
                .build();
        newsEntity = NewsEntity.builder().newsName("newsName").newsDescription("newsDescription")
                .newsId(UUID.randomUUID()).newsIconExternalUrl("newsIconStorageUrl")
                .newsBannerExternalUrl("newsIconExternalUrl").likes(100L).isRecommended(true)
                .newsIconStorageId(UUID.randomUUID())
                .tags("[\"tag1\", \"tag2\"]").build();
        newsPage = new PageImpl<>(List.of(newsEntity));
        programMobileResponseDto = ProgramMobileResponseDto.builder()
                .programName("programName").programAuthor("programAuthor").programDescription("programDescription")
                .programId(UUID.randomUUID()).comments("comments").programUserId(UUID.randomUUID())
                .programBannerExternalUrl("programBannerExternalUrl").rating(5F).ratingCount(100L).duration(1000L)
                .views(100L).build();
        programEntity = ProgramEntity.builder().programName("programName")
                .programAuthor("programAuthor").programDescription("programDescription")
                .programId(UUID.randomUUID()).comments("comments")
                .programBannerExternalUrl("programBannerExternalUrl")
                .programBannerStorageId(UUID.randomUUID())
                .rating(5F).ratingCount(100L).duration(1000L)
                .views(100L).build();
        programEntity.setCreatedBy(UUID.randomUUID());
        programPage = new PageImpl<>(List.of(programEntity));
        sectionMobileResponseDto = SectionMobileResponseDto.builder()
                .sectionName("sectionName").sectionDescription("sectionDescription")
                .sectionId(UUID.randomUUID()).numberOfLessons(10)
                .sectionOrder(1)
                .build();
        sectionEntity = SectionEntity.builder()
                .sectionName("sectionName").sectionDescription("sectionDescription")
                .sectionId(UUID.randomUUID()).numberOfLessons(10).programId(UUID.randomUUID())
                .sectionOrder(1).build();
        lessonMobileResponseDto = LessonMobileResponseDto.builder()
                .lessonName("lessonName").lessonDescription("lessonDescription")
                .lessonId(UUID.randomUUID()).lessonOrder(1).duration(1000L)
                .lessonExternalUrl("lessonExternalUrl").lessonText("lessonText")
                .build();
        lessonEntity = LessonEntity.builder()
                .lessonName("lessonName").lessonDescription("lessonDescription")
                .lessonId(UUID.randomUUID()).lessonOrder(1).duration(1000L)
                .lessonExternalUrl("lessonExternalUrl").lessonText("lessonText")
                .build();
        userEntity = UserEntity.builder()
                .userId(UUID.randomUUID()).userFirstName("userFirstName").userLastName("userLastName")
                .userMobile("+919999999999").userEmail("test@testmail.com").isMobileVerified(true).isEmailVerified(true)
                .orgId(UUID.randomUUID())
                .build();
        userResponseDto = UserResponseDto.builder()
                .userId(UUID.randomUUID()).userFirstName("userFirstName").userLastName("userLastName")
                .userMobile("+919999999999").userEmail("test@testmail.com").isMobileVerified(true).isEmailVerified(true)
                .orgId(UUID.randomUUID())
                .build();
        UserAoiOptionDto userAoiOptionDto = UserAoiOptionDto.builder()
                .id(1).isSelected(true).value("answer")
                .build();
        userAoiDto = UserAoiDto.builder()
                .questionId(1).questionName("questionName").max(2).options(List.of(userAoiOptionDto))
                .optionType(AppUtils.OptionType.MULTIPLE).status(AppUtils.UserAoiStatus.ACTIVE)
                .build();
        settingEntity = SettingEntity.builder()
                .settingId(UUID.randomUUID()).settingName("settingName")
                .settingValue("settingValue").settingStatus(AppUtils.SettingStatus.ACTIVE)
                .build();
        practiceUserRequestDto = PracticeUserRequestDto.builder()
                .practiceId(UUID.randomUUID()).userId(UUID.randomUUID())
                .resumeTime(100L).build();
        practiceUserEntity = PracticeUserEntity.builder()
                .practiceId(practiceUserRequestDto.getPracticeId()).userId(practiceUserRequestDto.getUserId())
                .practiceUserId(UUID.randomUUID()).resumeTime(100L).rating(5F).comments("comments")
                .practiceUserStatus(AppUtils.PracticeUserStatus.IN_PROGRESS).build();
        practiceUserRatingUpdateDto = PracticeUserRatingUpdateDto.builder()
                .rating(5F).comment("comments")
                .build();
        programUserRequestDto = ProgramUserRequestDto.builder()
                .programId(UUID.randomUUID()).userId(UUID.randomUUID())
                .build();
        programUserEntity = ProgramUserEntity.builder()
                .programId(UUID.randomUUID()).programUserId(UUID.randomUUID())
                .comments("comments").rating(5F).programUserId(UUID.randomUUID()).build();
        programUserEntity.setUserId(UUID.randomUUID());
        programUserRatingUpdateDto = ProgramUserRatingUpdateDto.builder()
                .comment("Comment").rating(5F).build();
        lessonUserEntity = LessonUserEntity.builder()
                .lessonId(UUID.randomUUID()).lessonUserId(UUID.randomUUID())
                .resumeTime(100L).userId(UUID.randomUUID()).build();
        lessonUserRequestDto = LessonUserRequestDto.builder()
                .lessonId(lessonEntity.getLessonId()).resumeTime(100L).userId(UUID.randomUUID()).build();
    }

    @Test
    void testGetPracticeCategoryList() {
        PracticeCategoryListDto mockDto = mock(PracticeCategoryListDto.class);
        when(mockDto.getPracticeCategoryIconStorageId()).thenReturn(UUID.randomUUID());
        when(practiceCategoryService.getPracticeCategoryList()).thenReturn(List.of(mockDto));
        ResponseEntity<AppResponseDto<List<PracticeCategoryListResponseDto>>> response = mobileController.getPracticeCategoryList();
        verify(practiceCategoryService, times(1)).getPracticeCategoryList();
        assert response != null;
        assert response.getBody() != null;
    }

    @Test
    void testGetPracticeCategoryList_IconExternalUrl() {
        PracticeCategoryListDto mockDto = mock(PracticeCategoryListDto.class);
        when(mockDto.getPracticeCategoryIconExternalUrl()).thenReturn("https://yoga.com");
        when(practiceCategoryService.getPracticeCategoryList()).thenReturn(List.of(mockDto));
        ResponseEntity<AppResponseDto<List<PracticeCategoryListResponseDto>>> response = mobileController.getPracticeCategoryList();
        verify(practiceCategoryService, times(1)).getPracticeCategoryList();
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPractices() {
        when(practiceService.getPractices(anyInt(), anyInt(), anyString(), any(), anyString(), any(UUID.class))).thenReturn(practicePage);
        when(practiceService.toPracticeMobileResponseDto(anyList())).thenReturn(List.of(practiceMobileResponseDto));
        ResponseEntity<AppResponseDto<List<PracticeMobileResponseDto>>> response = mobileController.getPractices(0, 10, "", Sort.Direction.ASC, "", UUID.randomUUID());
        verify(practiceService, times(1)).getPractices(anyInt(), anyInt(), anyString(), any(), anyString(), any(UUID.class));
        verify(practiceService, times(1)).toPracticeMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoems() {
        when(poemService.getPoems(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(poemPage);
        when(poemService.toPoemMobileResponseDto(anyList())).thenReturn(List.of(poemMobileResponseDto));
        ResponseEntity<AppResponseDto<List<PoemMobileResponseDto>>> response = mobileController.getPoems(0, 10, "", Sort.Direction.ASC, "");
        verify(poemService, times(1)).getPoems(anyInt(), anyInt(), anyString(), any(), anyString());
        verify(poemService, times(1)).toPoemMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetShorts() {
        when(shortsService.getShorts(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(shortsPage);
        when(shortsService.toShortsMobileResponseDto(anyList())).thenReturn(List.of(shortsMobileResponseDto));
        ResponseEntity<AppResponseDto<List<ShortsMobileResponseDto>>> response = mobileController.getShorts(0, 10, "", Sort.Direction.ASC, "");
        verify(shortsService, times(1)).getShorts(anyInt(), anyInt(), anyString(), any(), anyString());
        verify(shortsService, times(1)).toShortsMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetEvents() {
        when(eventService.getEvents(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(eventsPage);
        when(eventService.toEventMobileResponseDto(anyList())).thenReturn(List.of(eventMobileResponseDto));
        ResponseEntity<AppResponseDto<List<EventMobileResponseDto>>> response = mobileController.getEvents(0, 10, "", Sort.Direction.ASC, "");
        verify(eventService, times(1)).getEvents(anyInt(), anyInt(), anyString(), any(), anyString());
        verify(eventService, times(1)).toEventMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetNews() {
        when(newsService.getNews(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(newsPage);
        when(newsService.toNewsMobileResponseDto(anyList())).thenReturn(List.of(newsMobileResponseDto));
        ResponseEntity<AppResponseDto<List<NewsMobileResponseDto>>> response = mobileController.getNewsList(0, 10, "", Sort.Direction.ASC, "");
        verify(newsService, times(1)).getNews(anyInt(), anyInt(), anyString(), any(), anyString());
        verify(newsService, times(1)).toNewsMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateNewsViewCount() {
        doNothing().when(newsService).updateViewCount(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateNewsViewCount(newsEntity.getNewsId());
        verify(newsService, times(1)).updateViewCount(any());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testUpdateNewsLikeCount() {
        doNothing().when(newsService).updateLikeCount(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateNewsLikeCount(newsEntity.getNewsId());
        verify(newsService, times(1)).updateLikeCount(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testGetProgram() {
        when(programService.getPrograms(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(programPage);
        when(programService.toProgramMobileResponseDto(anyList())).thenReturn(List.of(programMobileResponseDto));
        ResponseEntity<AppResponseDto<List<ProgramMobileResponseDto>>> response = mobileController.getProgram(0, 10, "", Sort.Direction.ASC, "");
        verify(programService, times(1)).getPrograms(anyInt(), anyInt(), anyString(), any(), anyString());
        verify(programService, times(1)).toProgramMobileResponseDto(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetSectionsByProgramId() {
        when(sectionService.getAllSectionByProgramId(any(UUID.class))).thenReturn(List.of(sectionEntity));
        when(sectionService.toSectionMobileResponseDtos(anyList())).thenReturn(List.of(sectionMobileResponseDto));
        ResponseEntity<AppResponseDto<List<SectionMobileResponseDto>>> response = mobileController.getSectionsByProgramId(UUID.randomUUID());
        verify(sectionService, times(1)).getAllSectionByProgramId(any(UUID.class));
        verify(sectionService, times(1)).toSectionMobileResponseDtos(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetAllLessonsBySectionId() {
        when(lessonService.getAllLessonByProgramId(any(UUID.class))).thenReturn(List.of(lessonEntity));
        when(lessonService.toLessonMobileResponseDtos(anyList())).thenReturn(List.of(lessonMobileResponseDto));
        ResponseEntity<AppResponseDto<List<LessonMobileResponseDto>>> response = mobileController.getAllLessonsBySectionId(UUID.randomUUID());
        verify(lessonService, times(1)).getAllLessonByProgramId(any(UUID.class));
        verify(lessonService, times(1)).toLessonMobileResponseDtos(anyList());
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPrincipalUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserId).thenReturn(UUID.randomUUID());
            when(userService.getUserById(any(UUID.class))).thenReturn(userEntity);
            when(userService.getUserDto(any(UserEntity.class))).thenReturn(userResponseDto);
            ResponseEntity<AppResponseDto<UserResponseDto>> response = mobileController.getPrincipalUser();
            verify(userService, times(1)).getUserById(any(UUID.class));
            verify(userService, times(1)).getUserDto(any(UserEntity.class));
            assert response.getStatusCode() == HttpStatus.OK;
        }
    }

    @Test
    void testGetUserAOIQuestions() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils.readValue(anyString(), any())).thenReturn(List.of(userAoiDto));
            when(settingService.getSettingBySettingName(anyString())).thenReturn(settingEntity);
            ResponseEntity<AppResponseDto<List<UserAoiDto>>> response = mobileController.getUserAOIQuestions();
            verify(settingService, times(1)).getSettingBySettingName(anyString());
            assert response.getStatusCode() == HttpStatus.OK;
        }
    }

    @Test
    void testUpdateUserAOIQuestions() {
        doNothing().when(userService).updateUserAoiAnswers(anyString());
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateUserAOIQuestions("answer");
        verify(userService, times(1)).updateUserAoiAnswers(anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.SEARCH_FOUND.getMessage());
    }

    @Test
    void testChangePoemStatus() {
        doNothing().when(poemService).updateViewCount(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.changePoemStatus(UUID.randomUUID());
        verify(poemService, times(1)).updateViewCount(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePracticeUser() {
        when(practiceService.updatePracticeUser(any(PracticeUserRequestDto.class))).thenReturn(practiceUserEntity);
        when(practiceService.getPracticeById(any(UUID.class))).thenReturn(practiceEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("UserName");
        ResponseEntity<AppResponseDto<PracticeUserResponseDto>> response = mobileController.updatePracticeUser(practiceUserRequestDto);
        verify(practiceService, times(1)).updatePracticeUser(any(PracticeUserRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePracticeUserRating() {
        doNothing().when(practiceService).changeRatingAndComment(any(UUID.class), any(PracticeUserRatingUpdateDto.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updatePracticeUserRating(practiceUserEntity.getPracticeUserId(), practiceUserRatingUpdateDto);
        verify(practiceService).changeRatingAndComment(any(UUID.class), any(PracticeUserRatingUpdateDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePracticeUserStatus() {
        doNothing().when(practiceService).changePracticeUserStatus(any(UUID.class), any(AppUtils.PracticeUserStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updatePracticeUserStatus(practiceUserEntity.getPracticeUserId(), AppUtils.PracticeUserStatus.IN_PROGRESS);
        verify(practiceService, times(1)).changePracticeUserStatus(any(UUID.class), any(AppUtils.PracticeUserStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testShortsChangeViewCount() {
        doNothing().when(shortsService).updateViewCount(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.changeViewCount(shortsEntity.getShortsId());
        verify(shortsService, times(1)).updateViewCount(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testShortsChangeLikeCount() {
        doNothing().when(shortsService).updateLikeCount(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.changeLikeCount(shortsEntity.getShortsId());
        verify(shortsService, times(1)).updateLikeCount(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateProgramUser() {
        when(programService.updateProgramUser(any(ProgramUserRequestDto.class))).thenReturn(programUserEntity);
        when(programService.getProgramById(any(UUID.class))).thenReturn(programEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("UserName");
        ResponseEntity<AppResponseDto<ProgramUserResponseDto>> response = mobileController.updateProgramUser(programUserRequestDto);
        verify(programService, times(1)).updateProgramUser(any(ProgramUserRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateProgramUserRating() {
        doNothing().when(programService).changeRatingAndComment(any(UUID.class), any(ProgramUserRatingUpdateDto.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateProgramUserRating(programUserEntity.getProgramUserId(), programUserRatingUpdateDto);
        verify(programService, times(1)).changeRatingAndComment(any(UUID.class), any(ProgramUserRatingUpdateDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateProgramUserStatus() {
        doNothing().when(programService).changeProgramUserStatus(any(UUID.class), any(AppUtils.ProgramUserStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateProgramUserStatus(programUserEntity.getProgramUserId(), AppUtils.ProgramUserStatus.COMPLETE);
        verify(programService, times(1)).changeProgramUserStatus(any(UUID.class), any(AppUtils.ProgramUserStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateLessonUser() {
        when(lessonService.updateLessonUser(any(LessonUserRequestDto.class))).thenReturn(lessonUserEntity);
        when(lessonService.getLessonById(any(UUID.class))).thenReturn(lessonEntity);
        when(userService.getUserNameById(any(UUID.class))).thenReturn("UserName");
        ResponseEntity<AppResponseDto<LessonUserResponseDto>> response = mobileController.updateLessonUser(lessonUserRequestDto);
        verify(lessonService, times(1)).updateLessonUser(any(LessonUserRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateLessonUserStatus() {
        doNothing().when(lessonService).changeLessonUserStatus(any(UUID.class), any(AppUtils.LessonUserStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = mobileController.updateLessonUserStatus(lessonUserEntity.getLessonUserId(), AppUtils.LessonUserStatus.COMPLETE);
        verify(lessonService, times(1)).changeLessonUserStatus(any(UUID.class), any(AppUtils.LessonUserStatus.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetPoemDashBoard() {
        when(poemService.getTop3Poems()).thenReturn(List.of(poemEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<PoemMobileResponseDto>>> response = mobileController.getPoemDashboard();
        verify(poemService, times(1)).getTop3Poems();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testGetPracticeDashBoard() {
        when(practiceService.getTop3Practices()).thenReturn(List.of(practiceEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<PracticeMobileResponseDto>>> response = mobileController.getPracticeDashboard();
        verify(practiceService, times(1)).getTop3Practices();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testGetProgramDashBoard() {
        when(programService.getTop3Programs()).thenReturn(List.of(programEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<ProgramMobileResponseDto>>> response = mobileController.getProgramDashboard();
        verify(programService, times(1)).getTop3Programs();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testGetShortsDashBoard() {
        when(shortsService.getTop3Shorts()).thenReturn(List.of(shortsEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<ShortsMobileResponseDto>>> response = mobileController.getShortsDashboard();
        verify(shortsService, times(1)).getTop3Shorts();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testGetEventsDashBoard() {
        when(eventService.getTop3Events()).thenReturn(List.of(eventEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<EventMobileResponseDto>>> response = mobileController.getEventDashboard();
        verify(eventService, times(1)).getTop3Events();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testGetNewsDashBoard() {
        when(newsService.getTop3News()).thenReturn(List.of(newsEntity));
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("https://test.test/file");
        ResponseEntity<AppResponseDto<List<NewsMobileResponseDto>>> response = mobileController.getNewsDashboard();
        verify(newsService, times(1)).getTop3News();
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData() != null;
        assert response.getBody().getData().size() == 1;
    }
}
