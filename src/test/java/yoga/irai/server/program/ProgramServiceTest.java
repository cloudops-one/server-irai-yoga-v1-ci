package yoga.irai.server.program;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {
    @Mock
    private UserService userService;
    @Mock private StorageService storageService;
    @Mock private SettingService settingService;
    @Mock private LessonRepository lessonRepository;
    @Mock private ProgramRepository programRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private OrganizationService organizationService;
    @Mock private ProgramUserRepository programUserRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks
    private ProgramService programService;
    private ProgramEntity programEntity;
    private ProgramRequestDto requestDto;
    private UUID programId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        programId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        UUID bannerStorageId = UUID.randomUUID();
        requestDto = new ProgramRequestDto();
        requestDto.setProgramName("Yoga Program");
        requestDto.setProgramBannerStorageId(UUID.randomUUID());
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .orgId(orgId)
                .programBannerStorageId(bannerStorageId)
                .programName("Yoga Program")
                .tags(AppUtils.writeValueAsString(List.of("Tag1", "Tag2")))
                .flag(AppUtils.ProgramFlag.TRENDING)
                .build();
        programEntity.setCreatedBy(UUID.randomUUID());
        programEntity.setUpdatedBy(UUID.randomUUID());
    }
    private static MockedStatic<AppUtils> appUtilsStaticMock;

    @BeforeAll
    static void setupStaticMock() {
        appUtilsStaticMock = Mockito.mockStatic(AppUtils.class);
    }

    @AfterAll
    static void closeStaticMock() {
        appUtilsStaticMock.close();
    }

    @Test
    void addProgram_ShouldSave_WhenNameNotExists() {
        when(programRepository.existsByProgramName("Yoga Program")).thenReturn(false);
        when(programRepository.save(any())).thenReturn(programEntity);
        ProgramEntity saved = programService.addProgram(requestDto);
        assertNotNull(saved);
        verify(settingService, never()).updateSetting(any(), any());
        verify(programRepository).save(any());
    }
    @Test
    void addProgram_ShouldSaveProgram_WhenTagsProvided() {
        ProgramRequestDto dto = new ProgramRequestDto();
        dto.setProgramName("Yoga Advanced");
        dto.setTags(Set.of("Tag1", "Tag2"));
        ProgramEntity mappedEntity = new ProgramEntity();
        mappedEntity.setProgramId(UUID.randomUUID());
        Mockito.when(AppUtils.map(dto, ProgramEntity.class)).thenReturn(mappedEntity);
        Mockito.when(AppUtils.writeValueAsString(dto.getTags()))
                .thenReturn("[\"Tag1\",\"Tag2\"]");
        when(programRepository.existsByProgramName(dto.getProgramName())).thenReturn(false);
        when(programRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProgramEntity result = programService.addProgram(dto);
        assertNotNull(result);
        assertEquals("[\"Tag1\",\"Tag2\"]", result.getTags());
        verify(settingService).updateSetting(AppUtils.SettingName.PROGRAM_TAGS, dto.getTags());
        verify(programRepository).save(mappedEntity);
    }


    @Test
    void addProgram_ShouldThrow_WhenNameExists() {
        when(programRepository.existsByProgramName("Yoga Program")).thenReturn(true);
        assertThrows(AppException.class, () -> programService.addProgram(requestDto));
    }
    @Test
    void updateProgram_ShouldUpdateFields() {
        UUID bannerId = UUID.randomUUID();
        programEntity.setProgramBannerStorageId(bannerId);
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(programRepository.save(any())).thenReturn(programEntity);
        requestDto.setProgramBannerStorageId(UUID.randomUUID());
        ProgramEntity updated = programService.updateProgram(programId, requestDto);
        assertEquals(programId, updated.getProgramId());
        verify(programRepository).save(any());
    }
    @Test
    void updateProgram_shouldDeleteOldBannerStorage_whenConditionsMet() {
        UUID oldStorageId = UUID.randomUUID();
        UUID newStorageId = UUID.randomUUID();
        ProgramEntity program = ProgramEntity.builder()
                .programId(UUID.randomUUID())
                .programBannerExternalUrl(null)
                .programBannerStorageId(oldStorageId)
                .build();
        ProgramRequestDto dto = new ProgramRequestDto();
        dto.setProgramBannerStorageId(newStorageId);
        ProgramService spyService = Mockito.spy(programService);
        doReturn(program).when(spyService).getProgramById(program.getProgramId());
        when(programRepository.save(any())).thenReturn(program);
        spyService.updateProgram(program.getProgramId(), dto);
        verify(storageService).deleteStorageById(oldStorageId);
        verify(programRepository).save(program);
    }


    @Test
    void getProgramById_ShouldReturn_WhenExists() {
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        ProgramEntity found = programService.getProgramById(programId);
        assertEquals(programId, found.getProgramId());
    }
    @Test
    void getProgramById_ShouldThrow_WhenNotFound() {
        when(programRepository.findById(programId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> programService.getProgramById(programId));
    }
    @Test
    void getPrograms_ShouldReturnPage_ForMobileUser() {
        int page = 0, size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "programName"));
        String keyword = "yoga";
        Set<UUID> mobileOrgIds = Set.of(UUID.randomUUID());
        Page<ProgramEntity> expectedPage = new PageImpl<>(List.of(programEntity));
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
        when(organizationService.getOrgIdsForMobile()).thenReturn(mobileOrgIds);
        when(programRepository.search(keyword, mobileOrgIds, AppUtils.ProgramStatus.ACTIVE, pageable))
                .thenReturn(expectedPage);
        Page<ProgramEntity> result = programService.getPrograms(page, size, "programName", Sort.Direction.ASC, keyword);
        assertEquals(1, result.getContent().size());
        verify(programRepository).search(keyword, mobileOrgIds, AppUtils.ProgramStatus.ACTIVE, pageable);
    }

    @Test
    void getPrograms_ShouldReturnPage_ForPortalUser() {
        int page = 0, size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String keyword = "wellness";
        Set<UUID> orgIds = Set.of(orgId);
        Page<ProgramEntity> expectedPage = new PageImpl<>(List.of(programEntity));
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
        appUtilsStaticMock.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
        when(programRepository.search(keyword, orgIds, null, pageable)).thenReturn(expectedPage);
        Page<ProgramEntity> result = programService.getPrograms(page, size, "createdAt", Sort.Direction.DESC, keyword);
        assertEquals(1, result.getContent().size());
        verify(programRepository).search(keyword, orgIds, null, pageable);
    }

    @Test
    void getPrograms_ShouldReturnPage_ForKeycloakUser() {
        int page = 1, size = 5;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "programName"));
        String keyword = "fitness";
        Page<ProgramEntity> expectedPage = new PageImpl<>(List.of(programEntity));
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
        when(programRepository.search(keyword, null, null, pageable)).thenReturn(expectedPage);
        Page<ProgramEntity> result = programService.getPrograms(page, size, "programName", Sort.Direction.ASC, keyword);
        assertEquals(1, result.getContent().size());
        verify(programRepository).search(keyword, null, null, pageable);
    }

    @Test
    void deleteProgram_ShouldCascadeDeletes() {
        programEntity.setProgramBannerStorageId(UUID.randomUUID());
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(sectionRepository.getAllByProgramId(programId)).thenReturn(List.of(new SectionEntity()));
        programService.deleteProgram(programId);
        verify(programRepository).deleteById(programId);
        verify(storageService).deleteStorageByIds(anySet());
    }
    @Test
    void deleteProgram_shouldDeleteAllStorageAndLessons() {
        UUID bannerStorageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID lessonStorageId = UUID.randomUUID();
        ProgramEntity program = ProgramEntity.builder()
                .programId(programId)
                .programBannerStorageId(bannerStorageId)
                .build();
        ProgramService spyService = Mockito.spy(programService);
        doReturn(program).when(spyService).getProgramById(programId);
        SectionEntity section = SectionEntity.builder()
                .sectionId(sectionId)
                .programId(programId)
                .build();
        when(sectionRepository.getAllByProgramId(programId)).thenReturn(List.of(section));
        LessonEntity lesson = LessonEntity.builder()
                .lessonId(lessonId)
                .lessonStorageId(lessonStorageId)
                .build();
        when(lessonRepository.getAllBySectionId(sectionId)).thenReturn(List.of(lesson));
        spyService.deleteProgram(programId);
        verify(storageService).deleteStorageByIds(argThat(set ->
                set.contains(bannerStorageId) && set.contains(lessonStorageId)
        ));
        verify(lessonRepository).deleteAllById(Set.of(lessonId));
        verify(sectionRepository).deleteAll(List.of(section));
        verify(programRepository).deleteById(programId);
    }

    @Test
    void deleteProgram_ProgramEntityNull(){
        programEntity.setProgramBannerStorageId(null);
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(sectionRepository.getAllByProgramId(programId)).thenReturn(List.of(new SectionEntity()));
        programService.deleteProgram(programId);
        verify(programRepository).deleteById(programId);
    }
    @Test
    void updateProgramStatus_ShouldActivateAndNotify() {
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(programRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(organizationService.getOrgNameByOrgId(programEntity.getOrgId()))
                .thenReturn("OrgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(programEntity.getOrgId()))
                .thenReturn("OrgIconUrl");
        when(storageService.getStorageUrl(programEntity.getProgramBannerStorageId()))
                .thenReturn("BannerUrl");
        ProgramResponseDto mappedDto = new ProgramResponseDto();
        mappedDto.setProgramId(programEntity.getProgramId());
        mappedDto.setProgramName(programEntity.getProgramName());
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(mappedDto);
        programService.updateProgramStatus(programId, AppUtils.ProgramStatus.ACTIVE);
        verify(programRepository).save(programEntity);
    }
    @Test
    void updateProgramStatus_ShouldTriggerElseIfBranch_WhenExternalBannerUrlPresent() throws FirebaseMessagingException {
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(programRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(organizationService.getOrgNameByOrgId(programEntity.getOrgId())).thenReturn("OrgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(programEntity.getOrgId())).thenReturn("OrgIconUrl");
        when(organizationService.getTopicName()).thenReturn("ORG123");
        ProgramResponseDto mappedDto = new ProgramResponseDto();
        mappedDto.setProgramId(programEntity.getProgramId());
        mappedDto.setProgramName(programEntity.getProgramName());
        mappedDto.setProgramDescription("Calm and Focus");
        mappedDto.setProgramBannerStorageUrl(null);
        mappedDto.setProgramBannerExternalUrl("https://cdn.example.com/banner.jpg");
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(mappedDto);
        when(organizationService.getOrgNameByOrgId(programEntity.getOrgId())).thenReturn("OrgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(programEntity.getOrgId()))
                .thenReturn("OrgIconUrl");
        programService.updateProgramStatus(programId, AppUtils.ProgramStatus.ACTIVE);
        verify(programRepository).save(programEntity);
        verify(notificationService).sendNotificationToTopic(
                ("ORG123_PROGRAM"),
                (programEntity.getProgramName()),
                ("Calm and Focus"),
                ("https://cdn.example.com/banner.jpg"),
                (programEntity.getProgramId().toString())
        );
    }

    @Test
    void updateProgramStatus_Inactive(){
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        when(programRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProgramResponseDto mappedDto = new ProgramResponseDto();
        mappedDto.setProgramId(programEntity.getProgramId());
        mappedDto.setProgramName(programEntity.getProgramName());
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(mappedDto);
        programService.updateProgramStatus(programId, AppUtils.ProgramStatus.INACTIVE);
        verify(programRepository).save(programEntity);
    }

    @Test
    void updateProgramStatus_ShouldThrowAppException_WhenNotificationFails() throws FirebaseMessagingException {
        ProgramEntity program = ProgramEntity.builder()
                .programId(programId)
                .programStatus(AppUtils.ProgramStatus.INACTIVE)
                .build();
        ProgramService spyService = Mockito.spy(programService);
        doReturn(program).when(spyService).getProgramById(programId);
        ProgramResponseDto dto = ProgramResponseDto.builder()
                .programId(programId)
                .programName("Yoga Program")
                .programDescription("Desc")
                .build();
        doReturn(dto).when(spyService).getProgramResponseDto(program);
        when(organizationService.getTopicName()).thenReturn("YogaOrg");
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationService)
                .sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
        AppException exception = assertThrows(AppException.class, () ->
                spyService.updateProgramStatus(programId, AppUtils.ProgramStatus.ACTIVE));
        assertEquals("Notification failed", exception.getMessage());
    }

    @Test
    void updateProgramFlag_ShouldUpdateFlag() {
        when(programRepository.findById(programId)).thenReturn(Optional.of(programEntity));
        programService.updateProgramFlag(programId, AppUtils.ProgramFlag.TRENDING);
        verify(programRepository).save(programEntity);
        assertEquals(AppUtils.ProgramFlag.TRENDING, programEntity.getFlag());
    }
    @Test
    void getTotalPrograms_ShouldReturnCount_Keycloak_User() {
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType)
                .thenReturn(AppUtils.UserType.KEYCLOAK_USER);
        when(programRepository.count()).thenReturn(5L);
        Long result = programService.getTotalPrograms();
        assertEquals(5L, result);
        verify(programRepository).count();
    }
    @Test
    void getTotalPrograms_ShouldThrowForMobileUser() {
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType)
                .thenReturn(AppUtils.UserType.MOBILE_USER);
        assertThrows(AppException.class,
                () -> programService.getTotalPrograms());
    }
    @Test
    void getTotalPrograms_ShouldCountByOrgIdForPortalUser() {
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType)
                .thenReturn(AppUtils.UserType.PORTAL_USER);
        appUtilsStaticMock.when(AppUtils::getPrincipalOrgId)
                .thenReturn(orgId);
        when(programRepository.countByOrgId(orgId)).thenReturn(7L);
        Long result = programService.getTotalPrograms();
        assertEquals(7L, result);
        verify(programRepository).countByOrgId(orgId);
    }
    @Test
    void getTop3Programs_ShouldReturnPrograms() {
        appUtilsStaticMock.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
        appUtilsStaticMock.when(AppUtils::getPrincipalUserType)
                .thenReturn(AppUtils.UserType.PORTAL_USER);
        when(programRepository.getTop3ByOrgIdAndProgramStatusOrderByCreatedAtDesc(eq(orgId), any()))
                .thenReturn(List.of(programEntity));
        List<ProgramEntity> result = programService.getTop3Programs();
        assertEquals(1, result.size());
        assertSame(programEntity, result.getFirst());
    }
    @Test
    void updateProgramUser_ShouldCreateWhenNotFound() {
        UUID id = UUID.fromString("33d1b259-0e1b-4a83-ac12-bf21193eccd4");
        UUID userId = UUID.randomUUID();
        ProgramUserRequestDto dto = new ProgramUserRequestDto(userId , id);
        when(programUserRepository.getByProgramIdAndUserId(id , userId))
                .thenReturn(null);
        when(programUserRepository.save(any()))
                .thenAnswer(invocation -> {
                    ProgramUserEntity entity = invocation.getArgument(0);
                    entity.setProgramUserId(UUID.randomUUID());
                    return entity;
                });
        ProgramUserEntity saved = programService.updateProgramUser(dto);
        assertNotNull(saved);
        assertEquals(id, saved.getProgramId());
        assertEquals(userId, saved.getUserId());
        assertNotNull(saved.getProgramUserId());
    }

    @Test
    void updateProgramUser_shouldCreateNewEntity_whenNotFound() {
        UUID id = UUID.fromString("33d1b259-0e1b-4a83-ac12-bf21193eccd4");
        UUID userId = UUID.randomUUID();
        ProgramUserRequestDto dto = new ProgramUserRequestDto(userId , id);
        when(programUserRepository.getByProgramIdAndUserId(id , userId))
                .thenReturn(new ProgramUserEntity());
        when(programUserRepository.save(any()))
                .thenAnswer(invocation -> {
                    ProgramUserEntity entity = invocation.getArgument(0);
                    entity.setProgramUserId(UUID.randomUUID());
                    return entity;
                });
        ProgramUserEntity saved = programService.updateProgramUser(dto);
        assertNotNull(saved);
        assertEquals(id, saved.getProgramId());
        assertEquals(userId, saved.getUserId());
        assertNotNull(saved.getProgramUserId());
    }

    @Test
    void updateProgram_ShouldUpdateAllBranches() {
        ProgramRequestDto dto = new ProgramRequestDto();
        dto.setProgramBannerStorageId(UUID.randomUUID());
        dto.setTags(Set.of("Tag1", "Tag2"));
        ProgramEntity program = new ProgramEntity();
        program.setProgramId(programId);
        program.setProgramBannerExternalUrl(null);
        program.setProgramBannerStorageId(UUID.randomUUID());
        program.setTags("oldTag");
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        appUtilsStaticMock.when(() -> AppUtils.writeValueAsString(dto.getTags()))
                .thenReturn("[\"Tag1\",\"Tag2\"]");
        appUtilsStaticMock.when(() -> AppUtils.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(new HashSet<>(List.of("Tag1", "Tag2")));
        ProgramEntity updated = programService.updateProgram(programId, dto);
        verify(storageService, times(1)).deleteStorageById(any());
        verify(settingService, times(1)).updateSetting(AppUtils.SettingName.PROGRAM_TAGS, dto.getTags());
        assertEquals(dto.getProgramBannerStorageId(), updated.getProgramBannerStorageId());
        assertNull(updated.getProgramBannerExternalUrl());
        Set<String> updatedTags = AppUtils.readValue(updated.getTags(), new TypeReference<>() {});
        assertEquals(new HashSet<>(List.of("Tag1", "Tag2")), updatedTags);
    }


    @Test
    void updateProgram_ShouldSetExternalUrlWhenNoStorageId() {
        ProgramRequestDto dto = new ProgramRequestDto();
        dto.setProgramBannerExternalUrl("http://external.url");
        ProgramEntity program = new ProgramEntity();
        program.setProgramId(programId);
        program.setProgramBannerStorageId(null);
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProgramEntity updated = programService.updateProgram(programId, dto);
        assertEquals(dto.getProgramBannerExternalUrl(), updated.getProgramBannerExternalUrl());
        assertNull(updated.getProgramBannerStorageId());
    }

    @Test
    void changeProgramUserStatus_ShouldUpdateStatus() {
        UUID programUserId = UUID.randomUUID();
        ProgramUserEntity programUser = new ProgramUserEntity();
        when(programUserRepository.findById(programUserId)).thenReturn(Optional.of(programUser));
        programService.changeProgramUserStatus(programUserId, AppUtils.ProgramUserStatus.COMPLETE);
        assertEquals(AppUtils.ProgramUserStatus.COMPLETE, programUser.getProgramUserStatus());
        verify(programUserRepository).save(programUser);
    }
    @Test
    void changeRatingAndComment_ShouldUpdateRating() {
        UUID programUserId = UUID.randomUUID();
        ProgramUserEntity programUser = new ProgramUserEntity();
        programUser.setProgramId(programId);
        when(programUserRepository.findById(programUserId)).thenReturn(Optional.of(programUser));
        when(programUserRepository.findNonZeroRatingsByProgramId(programId)).thenReturn(List.of(4.0f));
        ProgramEntity program = new ProgramEntity();
        program.setProgramId(programId);
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProgramUserRatingUpdateDto dto = new ProgramUserRatingUpdateDto("Nice", 5.0f);
        programService.changeRatingAndComment(programUserId, dto);
        assertEquals(5.0f, programUser.getRating());
        assertEquals("Nice", programUser.getComments());
        verify(programUserRepository).save(programUser);
        verify(programRepository).save(program);
    }
    @Test
    void changeRatingAndComment_ShouldUpdateRating_BothBranches() {
        UUID programUserId = UUID.randomUUID();
        ProgramUserEntity programUserNoRating = new ProgramUserEntity();
        programUserNoRating.setProgramId(programId);
        programUserNoRating.setRating(null);
        when(programUserRepository.findById(programUserId)).thenReturn(Optional.of(programUserNoRating));
        when(programUserRepository.findNonZeroRatingsByProgramId(programId)).thenReturn(List.of(4.0f));
        ProgramEntity program = new ProgramEntity();
        program.setProgramId(programId);
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(programRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProgramUserRatingUpdateDto dto = new ProgramUserRatingUpdateDto("Excellent", 5.0f);
        programService.changeRatingAndComment(programUserId, dto);
        assertEquals(5.0f, programUserNoRating.getRating());
        assertEquals("Excellent", programUserNoRating.getComments());
        verify(programUserRepository).save(programUserNoRating);
        verify(programRepository).save(program);
        ProgramUserEntity programUserRated = new ProgramUserEntity();
        programUserRated.setProgramId(programId);
        programUserRated.setRating(3.0f);
        when(programUserRepository.findById(programUserId)).thenReturn(Optional.of(programUserRated));
        when(programUserRepository.findNonZeroRatingsByProgramId(programId)).thenReturn(List.of(3.0f, 5.0f));
        ProgramUserRatingUpdateDto dto2 = new ProgramUserRatingUpdateDto("Good", 4.0f);
        programService.changeRatingAndComment(programUserId, dto2);
        assertEquals(4.0f, programUserRated.getRating());
        assertEquals("Good", programUserRated.getComments());
        verify(programUserRepository, times(2)).save(any());
        verify(programRepository, times(2)).save(program);
    }
    @Test
    void testToProgramResponseDto() {
        List<ProgramEntity> entities = List.of(programEntity);
        Map<UUID, String> userNames = Map.of(
                programEntity.getCreatedBy(), "Creator",
                programEntity.getUpdatedBy(), "Updater"
        );
        when(userService.getUserData(entities)).thenReturn(userNames);
        Map<UUID, String> orgNames = Map.of(programEntity.getOrgId(), "OrgName");
        Map<UUID, String> orgIcons = Map.of(programEntity.getOrgId(), "OrgIconUrl");
        when(organizationService.getOrgNamesByIds(List.of(programEntity.getOrgId()))).thenReturn(orgNames);
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(List.of(programEntity.getOrgId()))).thenReturn(orgIcons);
        Map<UUID, String> signedUrls = Map.of(programEntity.getProgramBannerStorageId(), "BannerUrl");
        when(storageService.getSignedStorageUrlByIds(List.of(programEntity.getProgramBannerStorageId()))).thenReturn(signedUrls);
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(new ProgramResponseDto());
        appUtilsStaticMock.when(() -> AppUtils.readValue(eq("[\"tag1\",\"tag2\"]"), any(TypeReference.class)))
                .thenReturn(List.of("tag1", "tag2"));
        List<ProgramResponseDto> result = programService.toProgramResponseDto(entities);
        assertNotNull(result);
        assertEquals(1, result.size());
        ProgramResponseDto dto = result.getFirst();
        assertEquals("Creator", dto.getCreatedByName());
        assertEquals("Updater", dto.getUpdatedByName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("BannerUrl", dto.getProgramBannerStorageUrl());
        assertEquals("Trending", dto.getFlag());
    }
    @Test
    void testToProgramResponseDto_IfCoverage_TagsNotEmpty() {
        UUID bannerId = UUID.randomUUID();
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .orgId(orgId)
                .programBannerStorageId(bannerId)
                .programName("Yoga Program")
                .tags("[\"meditation\",\"fitness\"]")
                .flag(AppUtils.ProgramFlag.TRENDING)
                .build();
        programEntity.setCreatedBy(UUID.randomUUID());
        programEntity.setUpdatedBy(UUID.randomUUID());
        List<ProgramEntity> entities = List.of(programEntity);
        Map<UUID, String> userNames = Map.of(
                programEntity.getCreatedBy(), "Creator",
                programEntity.getUpdatedBy(), "Updater"
        );
        when(userService.getUserData(entities)).thenReturn(userNames);
        Map<UUID, String> orgNames = Map.of(orgId, "OrgName");
        Map<UUID, String> orgIcons = Map.of(orgId, "OrgIconUrl");
        when(organizationService.getOrgNamesByIds(List.of(orgId))).thenReturn(orgNames);
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(List.of(orgId))).thenReturn(orgIcons);
        Map<UUID, String> signedUrls = Map.of(bannerId, "BannerUrl");
        when(storageService.getSignedStorageUrlByIds(List.of(bannerId))).thenReturn(signedUrls);
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(new ProgramResponseDto());
        appUtilsStaticMock.when(() -> AppUtils.readValue(eq("[\"meditation\",\"fitness\"]"), any(TypeReference.class)))
                .thenReturn(Set.of("meditation", "fitness"));
        List<ProgramResponseDto> result = programService.toProgramResponseDto(entities);
        assertNotNull(result);
        assertEquals(1, result.size());
        ProgramResponseDto dto = result.getFirst();
        assertEquals("Creator", dto.getCreatedByName());
        assertEquals("Updater", dto.getUpdatedByName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("BannerUrl", dto.getProgramBannerStorageUrl());
        assertEquals("Trending", dto.getFlag());
    }

    @Test
    void testToProgramResponseDto_IfCoverage_EmptyTags() {
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .orgId(orgId)
                .programBannerStorageId(UUID.randomUUID())
                .programName("Yoga Program")
                .tags(null)
                .flag(AppUtils.ProgramFlag.TRENDING)
                .build();
        programEntity.setCreatedBy(UUID.randomUUID());
        programEntity.setUpdatedBy(UUID.randomUUID());
        List<ProgramEntity> entities = List.of(programEntity);
        Map<UUID, String> userNames = Map.of(
                programEntity.getCreatedBy(), "Creator",
                programEntity.getUpdatedBy(), "Updater"
        );
        when(userService.getUserData(entities)).thenReturn(userNames);
        Map<UUID, String> orgNames = Map.of(programEntity.getOrgId(), "OrgName");
        Map<UUID, String> orgIcons = Map.of(programEntity.getOrgId(), "OrgIconUrl");
        when(organizationService.getOrgNamesByIds(List.of(programEntity.getOrgId()))).thenReturn(orgNames);
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(List.of(programEntity.getOrgId()))).thenReturn(orgIcons);
        Map<UUID, String> signedUrls = Map.of(programEntity.getProgramBannerStorageId(), "BannerUrl");
        when(storageService.getSignedStorageUrlByIds(List.of(programEntity.getProgramBannerStorageId()))).thenReturn(signedUrls);
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(new ProgramResponseDto());
        List<ProgramResponseDto> result = programService.toProgramResponseDto(entities);
        assertNotNull(result);
        assertEquals(1, result.size());
        ProgramResponseDto dto = result.getFirst();
        assertEquals("Creator", dto.getCreatedByName());
        assertEquals("Updater", dto.getUpdatedByName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("BannerUrl", dto.getProgramBannerStorageUrl());
        assertEquals("Trending", dto.getFlag());
    }
    @Test
    void testToProgramResponseDto_IfCoverage_Flag() {
        programEntity = ProgramEntity.builder()
                .programId(programId)
                .orgId(orgId)
                .programBannerStorageId(UUID.randomUUID())
                .programName("Yoga Program")
                .tags(null)
                .flag(null)
                .build();
        programEntity.setCreatedBy(UUID.randomUUID());
        programEntity.setUpdatedBy(UUID.randomUUID());
        List<ProgramEntity> entities = List.of(programEntity);
        Map<UUID, String> userNames = Map.of(
                programEntity.getCreatedBy(), "Creator",
                programEntity.getUpdatedBy(), "Updater"
        );
        when(userService.getUserData(entities)).thenReturn(userNames);
        Map<UUID, String> orgNames = Map.of(programEntity.getOrgId(), "OrgName");
        Map<UUID, String> orgIcons = Map.of(programEntity.getOrgId(), "OrgIconUrl");
        when(organizationService.getOrgNamesByIds(List.of(programEntity.getOrgId()))).thenReturn(orgNames);
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(List.of(programEntity.getOrgId()))).thenReturn(orgIcons);
        Map<UUID, String> signedUrls = Map.of(programEntity.getProgramBannerStorageId(), "BannerUrl");
        when(storageService.getSignedStorageUrlByIds(List.of(programEntity.getProgramBannerStorageId()))).thenReturn(signedUrls);
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(new ProgramResponseDto());
        List<ProgramResponseDto> result = programService.toProgramResponseDto(entities);
        assertNotNull(result);
        assertEquals(1, result.size());
        ProgramResponseDto dto = result.getFirst();
        assertEquals("Creator", dto.getCreatedByName());
        assertEquals("Updater", dto.getUpdatedByName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("BannerUrl", dto.getProgramBannerStorageUrl());
    }

    @Test
    void toProgramMobileResponseDto_ShouldCoverAllBranches() {
        UUID programWithUserId = UUID.randomUUID();
        UUID programWithoutUserId = UUID.randomUUID();
        ProgramEntity programWithUser = ProgramEntity.builder()
                .programId(programWithUserId)
                .programBannerStorageId(UUID.randomUUID())
                .flag(AppUtils.ProgramFlag.TRENDING)
                .tags(AppUtils.writeValueAsString(List.of("yoga", "fitness")))
                .build();
        ProgramEntity programWithoutUser = ProgramEntity.builder()
                .programId(programWithoutUserId)
                .programBannerStorageId(null)
                .flag(null)
                .tags(null)
                .build();
        ProgramUserEntity programUserEntity = ProgramUserEntity.builder()
                .programUserId(UUID.randomUUID())
                .programUserStatus(AppUtils.ProgramUserStatus.IN_PROGRESS)
                .programId(programWithUserId)
                .userId(UUID.randomUUID())
                .build();
        Map<UUID, String> signedStorageMap = new HashMap<>();
        signedStorageMap.put(programWithUser.getProgramBannerStorageId(), "signedUrl");
        signedStorageMap.put(null, null);
        when(storageService.getSignedStorageUrlByIds(any())).thenReturn(signedStorageMap);
        appUtilsStaticMock.when(() -> AppUtils.map(any(ProgramEntity.class), eq(ProgramMobileResponseDto.class)))
                .thenAnswer(invocation -> {
                    ProgramEntity entity = invocation.getArgument(0);
                    ProgramMobileResponseDto dto = new ProgramMobileResponseDto();
                    dto.setProgramId(entity.getProgramId());
                    return dto;
                });
        when(programUserRepository.findByProgramIdAndUserId(eq(programWithUserId), any()))
                .thenReturn(Optional.of(programUserEntity));
        when(programUserRepository.findByProgramIdAndUserId(eq(programWithoutUserId), any()))
                .thenReturn(Optional.empty());
        List<ProgramMobileResponseDto> result =
                programService.toProgramMobileResponseDto(List.of(programWithUser, programWithoutUser));
        assertEquals(2, result.size());
        ProgramMobileResponseDto dtoWithUser = result.stream()
                .filter(dto -> dto.getProgramId().equals(programWithUserId))
                .findFirst()
                .orElseThrow();
        assertEquals("signedUrl", dtoWithUser.getProgramBannerStorageUrl());
        assertEquals(programUserEntity.getProgramUserId(), dtoWithUser.getProgramUserId());
        assertEquals(programUserEntity.getProgramUserStatus(), dtoWithUser.getProgramUserStatus());
        assertEquals("Trending", dtoWithUser.getFlag());
        ProgramMobileResponseDto dtoWithoutUser = result.stream()
                .filter(dto -> dto.getProgramId().equals(programWithoutUserId))
                .findFirst()
                .orElseThrow();
        assertNull(dtoWithoutUser.getProgramBannerStorageUrl());
        assertNull(dtoWithoutUser.getProgramUserId());
        assertNull(dtoWithoutUser.getProgramUserStatus());
        assertNull(dtoWithoutUser.getFlag());
        assertNull(dtoWithoutUser.getTags());
        verify(storageService, times(1)).getSignedStorageUrlByIds(any());
    }
    @Test
    void toProgramMobileResponseDto_ShouldCoverIfBranch() {
        UUID programWithUserId = UUID.randomUUID();
        UUID programWithoutUserId = UUID.randomUUID();
        ProgramEntity programWithUser = ProgramEntity.builder()
                .programId(programWithUserId)
                .programBannerStorageId(UUID.randomUUID())
                .flag(AppUtils.ProgramFlag.TRENDING)
                .tags("[\"meditation\",\"fitness\"]")
                .build();
        ProgramEntity programWithoutUser = ProgramEntity.builder()
                .programId(programWithoutUserId)
                .programBannerStorageId(null)
                .flag(null)
                .tags(null)
                .build();
        ProgramUserEntity programUserEntity = ProgramUserEntity.builder()
                .programUserId(UUID.randomUUID())
                .programUserStatus(AppUtils.ProgramUserStatus.IN_PROGRESS)
                .programId(programWithUserId)
                .userId(UUID.randomUUID())
                .build();
        Map<UUID, String> signedStorageMap = new HashMap<>();
        signedStorageMap.put(programWithUser.getProgramBannerStorageId(), "signedUrl");
        when(storageService.getSignedStorageUrlByIds(any())).thenReturn(signedStorageMap);
        appUtilsStaticMock.when(() -> AppUtils.map(any(ProgramEntity.class), eq(ProgramMobileResponseDto.class)))
                .thenAnswer(invocation -> {
                    ProgramEntity entity = invocation.getArgument(0);
                    ProgramMobileResponseDto dto = new ProgramMobileResponseDto();
                    dto.setProgramId(entity.getProgramId());
                    return dto;
                });
        when(programUserRepository.findByProgramIdAndUserId(eq(programWithUserId), any()))
                .thenReturn(Optional.of(programUserEntity));
        when(programUserRepository.findByProgramIdAndUserId(eq(programWithoutUserId), any()))
                .thenReturn(Optional.empty());
        appUtilsStaticMock.when(() -> AppUtils.readValue(eq("[\"meditation\",\"fitness\"]"), any(TypeReference.class)))
                .thenReturn(Set.of("meditation", "fitness"));
        List<ProgramMobileResponseDto> result =
                programService.toProgramMobileResponseDto(List.of(programWithUser, programWithoutUser));
        assertEquals(2, result.size());
        ProgramMobileResponseDto dtoWithUser = result.stream()
                .filter(dto -> dto.getProgramId().equals(programWithUserId))
                .findFirst()
                .orElseThrow();
        assertEquals("signedUrl", dtoWithUser.getProgramBannerStorageUrl());
        assertEquals(programUserEntity.getProgramUserId(), dtoWithUser.getProgramUserId());
        assertEquals(programUserEntity.getProgramUserStatus(), dtoWithUser.getProgramUserStatus());
        assertEquals("Trending", dtoWithUser.getFlag());
        assertEquals(Set.of("meditation", "fitness"), dtoWithUser.getTags());
        ProgramMobileResponseDto dtoWithoutUser = result.stream()
                .filter(dto -> dto.getProgramId().equals(programWithoutUserId))
                .findFirst()
                .orElseThrow();
        assertNull(dtoWithoutUser.getProgramBannerStorageUrl());
        assertNull(dtoWithoutUser.getProgramUserId());
        assertNull(dtoWithoutUser.getProgramUserStatus());
        assertNull(dtoWithoutUser.getFlag());
        assertNull(dtoWithoutUser.getTags());
        verify(storageService, times(1)).getSignedStorageUrlByIds(any());
    }


    @Test
    void getProgramResponseDto_ShouldMapAllFields() {
        ProgramResponseDto mappedDto = new ProgramResponseDto();
        mappedDto.setProgramId(UUID.randomUUID());
        mappedDto.setProgramName("Yoga Program");
        mappedDto.setFlag("Trending");
        appUtilsStaticMock.when(() -> AppUtils.map(programEntity, ProgramResponseDto.class))
                .thenReturn(mappedDto);
        when(organizationService.getOrgNameByOrgId(orgId)).thenReturn("OrgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(orgId)).thenReturn("OrgIconUrl");
        ProgramResponseDto dto = programService.getProgramResponseDto(programEntity);
        assertEquals("Yoga Program", dto.getProgramName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("OrgIconUrl", dto.getOrgIconStorageUrl());
        assertEquals("Trending", dto.getFlag());
    }
    @Test
    void getProgramResponseDto_ShouldMapFlag() {
        ProgramEntity program = ProgramEntity.builder()
                .programId(UUID.randomUUID())
                .programName("Yoga Program")
                .orgId(orgId)
                .flag(null)
                .build();
        ProgramResponseDto mappedDto = new ProgramResponseDto();
        mappedDto.setProgramId(program.getProgramId());
        mappedDto.setProgramName("Yoga Program");
        mappedDto.setFlag(null);
        appUtilsStaticMock.when(() -> AppUtils.map(program, ProgramResponseDto.class))
                .thenReturn(mappedDto);
        when(organizationService.getOrgNameByOrgId(orgId)).thenReturn("OrgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(orgId)).thenReturn("OrgIconUrl");
        ProgramResponseDto dto = programService.getProgramResponseDto(program);
        assertEquals("Yoga Program", dto.getProgramName());
        assertEquals("OrgName", dto.getOrgName());
        assertEquals("OrgIconUrl", dto.getOrgIconStorageUrl());
        assertNull(dto.getFlag());
        verify(organizationService, times(1)).getOrgNameByOrgId(orgId);
        verify(organizationService, times(1)).getOrgIconStorageIdToSignedIconUrl(orgId);
    }

}
