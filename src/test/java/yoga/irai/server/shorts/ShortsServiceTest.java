package yoga.irai.server.shorts;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.ShortsMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.shorts.user.ShortsUserEntity;
import yoga.irai.server.shorts.user.ShortsUserRepository;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortsServiceTest {
    @InjectMocks
    private ShortsService shortsService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private ShortsUserRepository shortsUserRepository;
    @Mock
    private ShortsRepository shortsRepository;
    @Mock
    private StorageService storageService;
    @Mock
    private SettingService settingService;
    private ShortsRequestDto requestDto;
    private ShortsEntity shortsEntity;
    private UUID shortsId;
    private UUID orgId;
    private UUID userId;
    private Pageable pageable;
    private ShortsUserEntity shortsUserEntity;
    private ShortsMobileResponseDto shortsMobileResponseDto;

    @BeforeEach
    void setUp() {
        shortsId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        userId = UUID.randomUUID();
        UUID oldStorageId = UUID.randomUUID();
        UUID oldBannerStorageId = UUID.randomUUID();
        UUID newStorageId = UUID.randomUUID();
        UUID newBannerStorageId = UUID.randomUUID();
        requestDto = new ShortsRequestDto();
        requestDto.setShortsName("Test Shorts");
        requestDto.setTags(Set.of("Test1", "Test2"));
        requestDto.setShortsStorageId(newStorageId);
        requestDto.setShortsBannerStorageId(newBannerStorageId);
        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        shortsEntity = ShortsEntity.builder().shortsId(shortsId).shortsName("Test Shorts1")
                .shortsStorageId(oldStorageId).shortsBannerStorageId(oldBannerStorageId)
                .shortsExternalUrl("https://www.google.com").shortsBannerExternalUrl("https://www.google1.com")
                .likes(5L).views(5L).shortsStatus(AppUtils.ShortsStatus.ACTIVE).tags("[\"TestTag1\",\"TestTag2\"]")
                .build();
        shortsUserEntity = ShortsUserEntity.builder()
                .shortsId(shortsId).userId(UUID.randomUUID())
                .shortsUserId(UUID.randomUUID())
                .likes(true).comments("Test")
                .build();
        shortsMobileResponseDto = ShortsMobileResponseDto.builder()
                .shortsId(shortsId).orgName("Test Org")
                .shortsExternalUrl("https://www.google.com")
                .shortsBannerExternalUrl("https://www.google1.com")
                .tags(Set.of("TestTag1", "TestTag2"))
                .likes(5L).views(5L).isLiked(true).comments("Test")
                .build();
    }

    @Test
    void addShorts_ShouldThrowException_WhenNameAlreadyExists() {
        when(shortsRepository.existsByShortsName("Test Shorts")).thenReturn(true);
        AppException exception = assertThrows(AppException.class,
                () -> shortsService.addShorts(requestDto));
        assertTrue(exception.getMessage().contains(AppUtils.Messages.NAME_EXISTS.getMessage()));
        verify(shortsRepository, never()).save(any());
    }

    @Test
    void addShorts_ShouldSaveShorts_WhenValidRequest() {
        when(shortsRepository.existsByShortsName("Test Shorts")).thenReturn(false);
        when(shortsRepository.save(any(ShortsEntity.class))).thenReturn(shortsEntity);
        ShortsEntity result = shortsService.addShorts(requestDto);
        assertNotNull(result);
        assertEquals("Test Shorts1", result.getShortsName());
        verify(settingService, times(1))
                .updateSetting((AppUtils.SettingName.SHORTS_TAGS), (requestDto.getTags()));
        verify(shortsRepository, times(1)).save(any(ShortsEntity.class));
    }

    @Test
    void addShorts_shouldSetStorageIdsToNull_whenEmpty() {
        ShortsRequestDto shorts = new ShortsRequestDto();
        shorts.setShortsName("MyShorts");
        shorts.setShortsStorageId(null);
        shorts.setShortsBannerStorageId(null);
        shorts.setTags(null);
        when(shortsRepository.existsByShortsName("MyShorts")).thenReturn(false);
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        ShortsEntity saved = shortsService.addShorts(shorts);
        assertNull(saved.getShortsStorageId());
        assertNull(saved.getShortsBannerStorageId());
        verify(shortsRepository).save(any(ShortsEntity.class));
        verify(settingService, never()).updateSetting(any(), any());
    }

    @Test
    void addShorts_ShouldNotSetTags_WhenTagsEmpty() {
        requestDto.setTags(null);
        when(shortsRepository.existsByShortsName("Test Shorts")).thenReturn(false);
        when(shortsRepository.save(any(ShortsEntity.class))).thenReturn(shortsEntity);
        ShortsEntity result = shortsService.addShorts(requestDto);
        assertNotNull(result);
        assertEquals("Test Shorts1", result.getShortsName());
    }

    @Test
    void changeShortsStatus_shouldUseExternalUrl_ifNotEmpty() throws FirebaseMessagingException {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStorageId(UUID.randomUUID())
                .shortsBannerExternalUrl("https://storage.url/banner.jpg")
                .shortsName("Test Shorts")
                .shortsDescription("Test Description")
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        when(organizationService.getTopicName()).thenReturn("ORG");
        ShortsResponseDto dto = ShortsResponseDto.builder()
                .shortsId(shortsId)
                .shortsName("Test Shorts")
                .shortsDescription("Test Description")
                .shortsBannerExternalUrl("https://storage.url/banner.jpg")
                .shortsBannerStorageUrl(null)
                .build();
        ShortsService spyService = spy(shortsService);
        doReturn(dto).when(spyService).getShortsResponseDto(any());
        spyService.changeShortsStatus(shortsId, AppUtils.ShortsStatus.ACTIVE);
        verify(notificationService).sendNotificationToTopic(
                ("ORG_" + AppUtils.ModuleType.SHORTS),
                ("Test Shorts"),
                ("Test Description"),
                ("https://storage.url/banner.jpg"),
                (shortsId.toString())
        );
    }

    @Test
    void updateShorts_ShouldDeleteOldStorageAndSaveUpdatedEntity() {
        UUID oldStorageId = shortsEntity.getShortsStorageId();
        UUID oldBannerStorageId = shortsEntity.getShortsBannerStorageId();
        shortsEntity.setShortsBannerExternalUrl(null);
        shortsEntity.setShortsExternalUrl(null);
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ShortsEntity result = shortsService.updateShorts(shortsId, requestDto);
        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(storageService, times(2)).deleteStorageById(captor.capture());
        List<UUID> deletedIds = captor.getAllValues();
        assertTrue(deletedIds.contains(oldStorageId));
        assertTrue(deletedIds.contains(oldBannerStorageId));
        verify(settingService, times(1))
                .updateSetting((AppUtils.SettingName.SHORTS_TAGS), (requestDto.getTags()));
        assertEquals(requestDto.getShortsName(), result.getShortsName());
        assertEquals(requestDto.getShortsStorageId(), result.getShortsStorageId());
        assertNull(result.getShortsExternalUrl());
        assertEquals(requestDto.getShortsBannerStorageId(), result.getShortsBannerStorageId());
        assertNull(result.getShortsBannerExternalUrl());
    }

    @Test
    void updateShorts_ShouldNotDeleteStorage_WhenIdsAreSame() {
        requestDto.setShortsStorageId(shortsEntity.getShortsStorageId());
        requestDto.setShortsBannerStorageId(shortsEntity.getShortsBannerStorageId());
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        ShortsEntity result = shortsService.updateShorts(shortsId, requestDto);
        verify(storageService, never()).deleteStorageById(any());
        verify(settingService).updateSetting((AppUtils.SettingName.SHORTS_TAGS), (requestDto.getTags()));
        assertEquals(requestDto.getShortsStorageId(), result.getShortsStorageId());
        assertEquals(requestDto.getShortsBannerStorageId(), result.getShortsBannerStorageId());
    }

    @Test
    void updateShorts_ShouldSetExternalUrl_WhenStorageIdIsNull() {
        requestDto.setShortsStorageId(null);
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShortsEntity result = shortsService.updateShorts(shortsId, requestDto);
        assertNull(result.getShortsExternalUrl());
        assertNull(result.getShortsStorageId());
    }

    @Test
    void updateShorts_ShouldUpdateTags_WhenTagsProvided() {
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        ShortsEntity result = shortsService.updateShorts(shortsId, requestDto);
        verify(settingService).updateSetting((AppUtils.SettingName.SHORTS_TAGS), (requestDto.getTags()));
        assertEquals(AppUtils.writeValueAsString(requestDto.getTags()), result.getTags());
    }

    @Test
    void updateShorts_shouldDeleteOldBannerStorage_whenAllConditionsTrue() {
        UUID oldId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        ShortsEntity entity = ShortsEntity.builder().shortsId(oldId).shortsBannerExternalUrl(null)
                .shortsBannerStorageId(oldId).build();
        ShortsRequestDto dto = new ShortsRequestDto();
        dto.setShortsBannerStorageId(newId);
        when(shortsRepository.findById(oldId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        shortsService.updateShorts(oldId, dto);
        verify(storageService).deleteStorageById(oldId);
    }

    @Test
    void updateShorts_shouldNotDeleteBannerStorage_whenExternalUrlPresent() {
        ShortsEntity entity = ShortsEntity.builder().shortsId(shortsId).shortsBannerExternalUrl("http://old-url")
                .shortsBannerStorageId(shortsId).build();
        ShortsRequestDto dto = new ShortsRequestDto();
        dto.setShortsBannerStorageId(UUID.randomUUID());
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        shortsService.updateShorts(shortsId, dto);
        verify(storageService, never()).deleteStorageById(any());
    }

    @Test
    void updateShorts_shouldNotDeleteBannerStorage_whenDtoStorageIdNull() {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsBannerExternalUrl(null)
                .shortsBannerStorageId(shortsId)
                .build();
        ShortsRequestDto dto = new ShortsRequestDto();
        dto.setShortsBannerStorageId(null);
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        shortsService.updateShorts(shortsId, dto);
        verify(storageService, never()).deleteStorageById(any());
    }

    @Test
    void updateShorts_shouldNotDeleteBannerStorage_whenIdsSame() {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsBannerExternalUrl(null)
                .shortsBannerStorageId(shortsId)
                .build();
        ShortsRequestDto dto = new ShortsRequestDto();
        dto.setShortsBannerStorageId(shortsId);
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        shortsService.updateShorts(shortsId, dto);
        verify(storageService, never()).deleteStorageById(any());
    }


    @Test
    void getShortsById_ShouldReturnEntity_WhenExists() {
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        ShortsEntity result = shortsService.getShortsById(shortsId);
        assertNotNull(result);
        assertEquals(shortsId, result.getShortsId());
        assertEquals("Test Shorts1", result.getShortsName());
        verify(shortsRepository, times(1)).findById(shortsId);
    }

    @Test
    void getShortsById_ShouldThrowException_WhenNotFound() {
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class,
                () -> shortsService.getShortsById(shortsId));
        assertTrue(exception.getMessage().contains(AppUtils.Messages.SHORTS_NOT_FOUND.getMessage()));
        verify(shortsRepository, times(1)).findById(shortsId);
    }

    @Test
    void getShorts_ShouldCallRepository_ForMobileUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            Set<UUID> orgIds = Set.of(UUID.randomUUID());
            lenient().when(organizationService.getOrgIdsForMobile()).thenReturn(orgIds);
            Page<ShortsEntity> expectedPage = new PageImpl<>(List.of(new ShortsEntity()));
            when(shortsRepository.search(
                    anyString(),
                    anySet(),
                    eq(AppUtils.ShortsStatus.ACTIVE),
                    any(Pageable.class)
            )).thenReturn(expectedPage);
            Page<ShortsEntity> result = shortsService.getShorts(0, 10, "createdAt", Sort.Direction.DESC, "keyword");
            assertEquals(expectedPage, result);
            verify(shortsRepository).search(
                    eq("keyword"),
                    anySet(),
                    eq(AppUtils.ShortsStatus.ACTIVE),
                    any(Pageable.class)
            );
        }
    }

    @Test
    void getShorts_ShouldCallRepository_ForPortalUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            Set<UUID> orgIds = Set.of(orgId);
            Page<ShortsEntity> expectedPage = new PageImpl<>(List.of(new ShortsEntity()));
            when(shortsRepository.search(anyString(), eq(orgIds), isNull(), eq(pageable)))
                    .thenReturn(expectedPage);
            Page<ShortsEntity> result = shortsService.getShorts(0, 10, "createdAt", Sort.Direction.DESC, "keyword");
            assertEquals(expectedPage, result);
            verify(shortsRepository).search("keyword", orgIds, null, pageable);
        }
    }

    @Test
    void getShorts_ShouldCallRepository_ForKeycloakUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(UUID.randomUUID());
            Page<ShortsEntity> expectedPage = new PageImpl<>(List.of(new ShortsEntity()));
            when(shortsRepository.search(anyString(), isNull(), isNull(), eq(pageable)))
                    .thenReturn(expectedPage);
            Page<ShortsEntity> result = shortsService.getShorts(0, 10, "createdAt", Sort.Direction.DESC, "keyword");
            assertEquals(expectedPage, result);
            verify(shortsRepository).search("keyword", null, null, pageable);
        }
    }

    @Test
    void deleteShorts_shouldHitIfBranch() {
        UUID storageId = UUID.randomUUID();
        UUID bannerStorageId = UUID.randomUUID();
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStorageId(storageId)
                .shortsBannerStorageId(bannerStorageId)
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        shortsService.deleteShorts(shortsId);
        verify(storageService).deleteStorageByIds(argThat(set ->
                set.contains(storageId) && set.contains(bannerStorageId) && set.size() == 2));
        verify(shortsRepository).deleteById(shortsId);
    }

    @Test
    void deleteShorts_shouldCallDeleteStorageByIds_whenStorageIdsNotEmpty() {
        UUID shortsStorageId = UUID.randomUUID();
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStorageId(shortsStorageId)
                .shortsBannerStorageId(null)
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        shortsService.deleteShorts(shortsId);
        verify(storageService).deleteStorageByIds(argThat(ids -> ids.contains(shortsStorageId)));
        verify(shortsRepository).deleteById(shortsId);
    }

    @Test
    void deleteShorts_ShouldDeleteStorageAndRemoveShorts() {
        UUID shortsStorageId = UUID.randomUUID();
        UUID shortsBannerStorageId = UUID.randomUUID();
        ShortsEntity shorts = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStorageId(shortsStorageId)
                .shortsBannerStorageId(shortsBannerStorageId)
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shorts));
        shortsService.deleteShorts(shortsId);
        ArgumentCaptor<Set<UUID>> captor = ArgumentCaptor.forClass(Set.class);
        verify(storageService, times(1)).deleteStorageByIds(captor.capture());
        Set<UUID> deletedIds = captor.getValue();
        assertTrue(deletedIds.contains(shortsStorageId));
        assertTrue(deletedIds.contains(shortsBannerStorageId));
        verify(shortsRepository, times(1)).deleteById(shortsId);
    }

    @Test
    void changeShortsStatus_shouldUseBannerStorageUrl_ifNotEmpty() throws FirebaseMessagingException {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStorageId(UUID.randomUUID())
                .shortsExternalUrl(null)
                .shortsBannerExternalUrl(null)
                .shortsName("Test Shorts")
                .shortsDescription("Test Description")
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any())).thenReturn(entity);
        when(organizationService.getTopicName()).thenReturn("ORG");
        ShortsResponseDto dto = ShortsResponseDto.builder()
                .shortsId(shortsId)
                .shortsName("Test Shorts")
                .shortsDescription("Test Description")
                .shortsBannerStorageUrl("https://storage.url/banner.jpg")
                .shortsBannerExternalUrl(null)
                .build();
        ShortsService spyService = spy(shortsService);
        doReturn(dto).when(spyService).getShortsResponseDto(any());
        spyService.changeShortsStatus(shortsId, AppUtils.ShortsStatus.ACTIVE);
        verify(notificationService).sendNotificationToTopic(
                ("ORG_" + AppUtils.ModuleType.SHORTS),
                ("Test Shorts"),
                ("Test Description"),
                ("https://storage.url/banner.jpg"),
                (shortsId.toString())
        );
    }

    @Test
    void sendShortsNotification_shouldThrowAppException_whenNotificationFails() throws FirebaseMessagingException {
        ShortsResponseDto dto = new ShortsResponseDto();
        dto.setShortsId(UUID.randomUUID());
        dto.setShortsName("Test Shorts");
        dto.setShortsDescription("Test Description");
        dto.setShortsBannerStorageUrl(null);
        dto.setShortsBannerExternalUrl(null);
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationService).sendNotificationToTopic(
                        anyString(), anyString(), anyString(), anyString(), anyString());
        AppException thrown = assertThrows(AppException.class, () ->
                ReflectionTestUtils.invokeMethod(shortsService, "sendShortsNotification", dto)
        );
        assertEquals("Notification failed", thrown.getMessage());
    }

    @Test
    void changeShortsStatus_ShouldUpdateStatusAndSendNotification_WhenActive() {
        ShortsEntity entity = ShortsEntity.builder()
                .shortsId(shortsId)
                .shortsStatus(AppUtils.ShortsStatus.INACTIVE)
                .build();
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(entity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        shortsService.changeShortsStatus(shortsId, AppUtils.ShortsStatus.ACTIVE);
        assertEquals(AppUtils.ShortsStatus.ACTIVE, entity.getShortsStatus());
        verify(shortsRepository, times(1)).save(entity);
    }

    @Test
    void changeShortsStatus_ShouldUpdateStatusAndNotSendNotification_WhenInactive() {
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        shortsService.changeShortsStatus(shortsId, AppUtils.ShortsStatus.INACTIVE);
        assertEquals(AppUtils.ShortsStatus.INACTIVE, shortsEntity.getShortsStatus());
        verify(shortsRepository, times(1)).save(shortsEntity);
    }

    @Test
    void updateViewCount_ShouldIncrementViewsAndSaveEntity() {
        when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
        when(shortsRepository.save(any(ShortsEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        shortsService.updateViewCount(shortsId);
        assertEquals(6L, shortsEntity.getViews());
        verify(shortsRepository, times(1)).save(shortsEntity);
    }

    @Test
    void updateLikeCount_ShouldIncrementLikes() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            when(shortsUserRepository.findShortsUserEntityByShortsIdAndUserId(any(UUID.class), any(UUID.class)))
                    .thenReturn(shortsUserEntity);
            when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
            when(shortsRepository.save(any(ShortsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            shortsService.updateLikeCount(shortsId);
            assertEquals(6L, shortsEntity.getLikes());
            verify(shortsRepository).save(shortsEntity);
        }
    }

    @Test
    void updateLikeCount_ShouldIncrementLikes_NullShortUsersEntity() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            when(shortsUserRepository.findShortsUserEntityByShortsIdAndUserId(any(UUID.class), any(UUID.class)))
                    .thenReturn(null);
            when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
            when(shortsRepository.save(any(ShortsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            shortsService.updateLikeCount(shortsId);
            assertEquals(6L, shortsEntity.getLikes());
            verify(shortsRepository).save(shortsEntity);
        }
    }

    @Test
    void updateComment() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            when(shortsUserRepository.findShortsUserEntityByShortsIdAndUserId(any(UUID.class), any(UUID.class)))
                    .thenReturn(shortsUserEntity);
            when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
            when(shortsRepository.save(any(ShortsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            shortsService.updateComment(shortsId, "test");
            assertEquals(6L, shortsEntity.getLikes());
            verify(shortsRepository).save(shortsEntity);
        }
    }

    @Test
    void updateComment_NullShortsUserEntity() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            when(shortsUserRepository.findShortsUserEntityByShortsIdAndUserId(any(UUID.class), any(UUID.class)))
                    .thenReturn(null);
            when(shortsRepository.findById(shortsId)).thenReturn(Optional.of(shortsEntity));
            when(shortsRepository.save(any(ShortsEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            shortsService.updateComment(shortsId, "test");
            assertEquals(6L, shortsEntity.getLikes());
            verify(shortsRepository).save(shortsEntity);
        }
    }

    @Test
    void getTotalShorts_ShouldReturnCount_ForKeycloakUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(shortsRepository.count()).thenReturn(42L);
            Long total = shortsService.getTotalShorts();
            assertEquals(42L, total);
            verify(shortsRepository).count();
        }
    }

    @Test
    void getTotalShorts_ShouldReturnCount_ForPortalUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(shortsRepository.countByOrgId(orgId)).thenReturn(10L);
            Long total = shortsService.getTotalShorts();
            assertEquals(10L, total);
            verify(shortsRepository).countByOrgId(orgId);
        }
    }

    @Test
    void getTotalShorts_ShouldThrowException_ForMobileUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            AppException exception = assertThrows(AppException.class, () -> shortsService.getTotalShorts());
            assertEquals(AppUtils.Messages.ACCESS_DENIED.getMessage(), exception.getMessage());
        }
    }

    @Test
    void getTop3Shorts_ShouldReturnTop3ActiveShorts() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            List<ShortsEntity> mockShorts = List.of(
                    new ShortsEntity(), new ShortsEntity(), new ShortsEntity()
            );
            when(shortsRepository.getTop3ByOrgIdAndShortsStatusOrderByCreatedAtDesc(
                    orgId, AppUtils.ShortsStatus.ACTIVE))
                    .thenReturn(mockShorts);
            List<ShortsEntity> result = shortsService.getTop3Shorts();
            assertEquals(3, result.size());
            assertEquals(mockShorts, result);
            verify(shortsRepository)
                    .getTop3ByOrgIdAndShortsStatusOrderByCreatedAtDesc(orgId, AppUtils.ShortsStatus.ACTIVE);
        }
    }
    @Test
    void toShortsResponseDto_ShouldMapEntitiesToDtoCorrectly() {
        UUID shortsStorageId = UUID.randomUUID();
        UUID shortsBannerStorageId = UUID.randomUUID();
        shortsEntity.setShortsStorageId(shortsStorageId);
        shortsEntity.setShortsBannerStorageId(shortsBannerStorageId);
        shortsEntity.setShortsName("Test Shorts1");
        List<ShortsEntity> entities = List.of(shortsEntity);
        Map<UUID, String> storageMap = Map.of(
                shortsStorageId, "signedUrl1",
                shortsBannerStorageId, "signedUrl2"
        );
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(storageMap);
        List<ShortsResponseDto> shortsDto = shortsService.toShortsResponseDto(entities);
        assertEquals(1, shortsDto.size());
        ShortsResponseDto dto = shortsDto.getFirst();
        assertEquals("Test Shorts1", dto.getShortsName());
        assertEquals("signedUrl1", dto.getShortsStorageUrl());
        assertEquals("signedUrl2", dto.getShortsBannerStorageUrl());
        assertNull(dto.getCreatedByName());
        assertNull(dto.getUpdatedByName());
    }
    // Java
    @Test
    void toShortsMobileResponseDto_ShouldMapEntitiesToDtoCorrectly() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            UUID shortsStorageId = UUID.randomUUID();
            UUID shortsBannerStorageId = UUID.randomUUID();
            ShortsEntity entity = ShortsEntity.builder()
                    .shortsId(UUID.randomUUID())
                    .shortsStorageId(shortsStorageId)
                    .shortsBannerStorageId(shortsBannerStorageId)
                    .shortsName("Mobile Shorts")
                    .build();
            List<ShortsEntity> entities = List.of(entity);
            Map<UUID, String> storageMap = Map.of(
                    shortsStorageId, "mobileSignedUrl1",
                    shortsBannerStorageId, "mobileSignedUrl2"
            );
            utilities.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            utilities.when(() -> AppUtils.map(any(ShortsEntity.class), eq(ShortsMobileResponseDto.class)))
                    .thenReturn(shortsMobileResponseDto);
            when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(storageMap);
            when(shortsUserRepository.findShortsUserEntityByShortsIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(shortsUserEntity);
            List<ShortsMobileResponseDto> resultList = shortsService.toShortsMobileResponseDto(entities);
            assertEquals(1, resultList.size());
        }
    }
}
