package yoga.irai.server.practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.PracticeMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.practice.category.PracticeCategoryService;
import yoga.irai.server.practice.user.PracticeUserEntity;
import yoga.irai.server.practice.user.PracticeUserRatingUpdateDto;
import yoga.irai.server.practice.user.PracticeUserRepository;
import yoga.irai.server.practice.user.PracticeUserRequestDto;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private SettingService settingService;

    @Mock
    private StorageService storageService;

    @Mock
    private PracticeRepository practiceRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PracticeUserRepository practiceUserRepository;

    @Mock
    private PracticeCategoryService practiceCategoryService;

    @InjectMocks
    private PracticeService practiceService;

    private UUID practiceId;
    private UUID orgId;
    private UUID practiceCategoryId;
    private UUID practiceUserId;
    private UUID practiceStorageId;
    private UUID practiceIconStorageId;
    private UUID practiceBannerStorageId;
    private UUID updatedPracticeStorageId;
    private UUID updatedPracticeIconStorageId;
    private UUID updatedPracticeBannerStorageId;
    private String practiceBannerExternalUrl;
    private String updatedPracticeExternalUrl;
    private String updatedPracticeIconExternalUrl;
    private String updatedPracticeBannerExternalUrl;
    private PracticeEntity practiceEntity;
    private PracticeRequestDto practiceRequestDto;
    private PracticeUserEntity practiceUserEntity;
    private PracticeUserRequestDto practiceUserRequestDto;
    private Page<PracticeEntity> mockPage;

    @BeforeEach
    void setUp() {
        practiceId = UUID.randomUUID();
        orgId = UUID.randomUUID();
        practiceUserId = UUID.randomUUID();
        practiceCategoryId = UUID.randomUUID();
        practiceStorageId = UUID.randomUUID();
        practiceIconStorageId = UUID.randomUUID();
        practiceBannerStorageId = UUID.randomUUID();
        updatedPracticeStorageId = UUID.randomUUID();
        updatedPracticeIconStorageId = UUID.randomUUID();
        updatedPracticeBannerStorageId = UUID.randomUUID();
        practiceBannerExternalUrl = "https://test.test/banner";
        updatedPracticeExternalUrl = "https://test.test/updatedFile";
        updatedPracticeIconExternalUrl = "https://test.test/updatedIcon";
        updatedPracticeBannerExternalUrl = "https://test.test/updatedBanner";
        practiceEntity = PracticeEntity.builder()
                .practiceId(practiceId).orgId(orgId).practiceName("Test Practice").practiceDescription("Test Practice Description")
                .practiceCategoryId(practiceCategoryId).practiceIconStorageId(UUID.randomUUID())
                .practiceBannerStorageId(UUID.randomUUID()).practiceStorageId(UUID.randomUUID())
                .duration(1000L).tags("[\"tag1\",\"tag2\"]").rating(5F).ratingCount(1000L)
                .build();
        practiceEntity.setCreatedBy(practiceUserId);
        practiceEntity.setUpdatedBy(practiceUserId);
        practiceRequestDto = PracticeRequestDto.builder()
                .practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                .practiceStorageId(practiceEntity.getPracticeStorageId()).practiceIconStorageId(practiceEntity.getPracticeIconStorageId())
                .practiceBannerStorageId(practiceEntity.getPracticeBannerStorageId()).duration(practiceEntity.getDuration())
                .tags(Set.of("tag1", "tag2")).practiceCategoryId(practiceEntity.getPracticeCategoryId()).build();
        practiceUserEntity = PracticeUserEntity.builder().practiceUserId(practiceUserId).practiceId(practiceId).userId(practiceUserId)
                .comments("Comments").rating(5F).resumeTime(500L)
                .build();
        practiceUserRequestDto = PracticeUserRequestDto.builder().practiceId(practiceId).userId(practiceUserId).resumeTime(500L).build();
        mockPage = new PageImpl<>(List.of(practiceEntity));
    }

    @Test
    void testAddPractice_Success() {
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.existsByPracticeName(practiceRequestDto.getPracticeName()))
                .thenReturn(false);
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.addPractice(practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        verify(practiceRepository, times(1)).existsByPracticeName(anyString());
        assert practice != null;
        assert practice.getPracticeId().equals(practiceId);
    }

    @Test
    void testAddPractice_Success_NoTags() {
        practiceRequestDto.setTags(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.existsByPracticeName(practiceRequestDto.getPracticeName()))
                .thenReturn(false);
        PracticeEntity practice = practiceService.addPractice(practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        verify(practiceRepository, times(1)).existsByPracticeName(anyString());
        assert practice != null;
        assert practice.getPracticeId().equals(practiceId);
    }

    @Test
    void testAddPractice_Success_NoStorageIds() {
        practiceRequestDto.setPracticeStorageId(null);
        practiceRequestDto.setPracticeIconStorageId(null);
        practiceRequestDto.setPracticeBannerStorageId(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.existsByPracticeName(practiceRequestDto.getPracticeName()))
                .thenReturn(false);
        PracticeEntity practice = practiceService.addPractice(practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        verify(practiceRepository, times(1)).existsByPracticeName(anyString());
        assert practice != null;
        assert practice.getPracticeId().equals(practiceId);
    }

    @Test
    void testAddPractice_Failure() {
        when(practiceRepository.existsByPracticeName(practiceRequestDto.getPracticeName()))
                .thenReturn(true);
        AppException ex = assertThrows(AppException.class, () ->
                practiceService.addPractice(practiceRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(practiceRepository, never()).save(any(PracticeEntity.class));
    }

    @Test
    void testUpdatePractice_Success() {
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeId().equals(practiceId);
    }

    @Test
    void testUpdatePractice_Success_NoTags() {
        practiceRequestDto.setTags(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeId().equals(practiceId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageId_NoChange() {
        practiceRequestDto.setPracticeIconStorageId(practiceIconStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeIconStorageId().equals(practiceIconStorageId);
    }

    @Test
    void testUpdatePractice_Success_BannerStorageId_NoChange() {
        practiceRequestDto.setPracticeBannerStorageId(practiceBannerStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeBannerStorageId().equals(practiceBannerStorageId);
    }

    @Test
    void testUpdatePractice_Success_StorageId_NoChange() {
        practiceRequestDto.setPracticeStorageId(practiceStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeStorageId().equals(practiceStorageId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageId() {
        practiceRequestDto.setPracticeIconStorageId(updatedPracticeIconStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeIconStorageId().equals(updatedPracticeIconStorageId);
    }

    @Test
    void testUpdatePractice_Success_BannerStorageId() {
        practiceRequestDto.setPracticeBannerStorageId(updatedPracticeBannerStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeBannerStorageId().equals(updatedPracticeBannerStorageId);
    }

    @Test
    void testUpdatePractice_Success_StorageId() {
        practiceRequestDto.setPracticeStorageId(updatedPracticeStorageId);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeStorageId().equals(updatedPracticeStorageId);
    }

    @Test
    void testUpdatePractice_Success_IconStorageUrl_NoChange() {
        practiceRequestDto.setPracticeIconExternalUrl(updatedPracticeIconExternalUrl);
        practiceRequestDto.setPracticeIconStorageId(null);
        practiceEntity.setPracticeIconExternalUrl(updatedPracticeIconExternalUrl);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeIconExternalUrl().equals(updatedPracticeIconExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_BannerStorageUrl_NoChange() {
        practiceRequestDto.setPracticeBannerExternalUrl(updatedPracticeBannerExternalUrl);
        practiceRequestDto.setPracticeBannerStorageId(null);
        practiceEntity.setPracticeBannerExternalUrl(updatedPracticeBannerExternalUrl);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeBannerExternalUrl().equals(updatedPracticeBannerExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_StorageUrl_NoChange() {
        practiceRequestDto.setPracticeExternalUrl(updatedPracticeExternalUrl);
        practiceRequestDto.setPracticeStorageId(null);
        practiceEntity.setPracticeExternalUrl(updatedPracticeExternalUrl);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeExternalUrl().equals(updatedPracticeExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_IconStorageUrl() {
        practiceRequestDto.setPracticeIconExternalUrl(updatedPracticeIconExternalUrl);
        practiceRequestDto.setPracticeIconStorageId(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeIconExternalUrl().equals(updatedPracticeIconExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_BannerStorageUrl() {
        practiceRequestDto.setPracticeBannerExternalUrl(updatedPracticeBannerExternalUrl);
        practiceRequestDto.setPracticeBannerStorageId(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeBannerExternalUrl().equals(updatedPracticeBannerExternalUrl);
    }

    @Test
    void testUpdatePractice_Success_StorageUrl() {
        practiceRequestDto.setPracticeExternalUrl(updatedPracticeExternalUrl);
        practiceRequestDto.setPracticeStorageId(null);
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        when(practiceRepository.findById(practiceId)).thenReturn(Optional.of(practiceEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PracticeEntity practice = practiceService.updatePractice(practiceId, practiceRequestDto);

        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        assert practice != null;
        assert practice.getPracticeExternalUrl().equals(updatedPracticeExternalUrl);
    }

    @Test
    void testGetPractices_MobileUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            when(practiceRepository.searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            when(organizationService.getOrgIdsForMobile()).thenReturn(Set.of(orgId));
            Page<PracticeEntity> practices = practiceService.getPractices(0, 10, "CreatedAt", Sort.Direction.ASC, "", practiceCategoryId);
            verify(practiceRepository, times(1)).searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class));
            assert practices != null;
            assert !practices.getContent().isEmpty();
        }
    }

    @Test
    void testGetPractices_PortalUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(practiceRepository.searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<PracticeEntity> practices = practiceService.getPractices(0, 10, "CreatedAt", Sort.Direction.ASC, "", practiceCategoryId);
            verify(practiceRepository, times(1)).searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class));
            assertNotNull(practices);
            assertNotNull(practices.getContent());
        }
    }

    @Test
    void testGetPractices_AdminUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(practiceRepository.searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<PracticeEntity> practices = practiceService.getPractices(0, 10, "CreatedAt", Sort.Direction.ASC, "", practiceCategoryId);
            verify(practiceRepository, times(1)).searchDynamic(any(UUID.class), anyString(), any(), any(), any(Pageable.class));
            assertNotNull(practices);
            assertNotNull(practices.getContent());
        }
    }

    @Test
    void testDeletePractices() {
        practiceEntity.setPracticeBannerStorageId(practiceBannerStorageId);
        practiceEntity.setPracticeIconStorageId(practiceIconStorageId);
        practiceEntity.setPracticeStorageId(practiceStorageId);
        when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
        doNothing().when(practiceRepository).deleteById(any(UUID.class));
        doNothing().when(storageService).deleteStorageByIds(anySet());
        practiceService.delete(practiceId);
        verify(practiceRepository, times(1)).deleteById(any(UUID.class));
        verify(practiceRepository).findById(any(UUID.class));
    }

    @Test
    void testDeletePractices_NoStorageId() {
        practiceEntity.setPracticeBannerStorageId(null);
        practiceEntity.setPracticeIconStorageId(null);
        practiceEntity.setPracticeStorageId(null);
        when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
        doNothing().when(practiceRepository).deleteById(any(UUID.class));
        practiceService.delete(practiceId);
        verify(practiceRepository, times(1)).deleteById(any(UUID.class));
        verify(practiceRepository).findById(any(UUID.class));
    }

    @Test
    void testChangePracticeStatus_Active_storageUrl() {
        try {
            practiceEntity.setPracticeBannerExternalUrl(null);
            practiceEntity.setPracticeBannerStorageId(practiceBannerStorageId);
            when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(practiceBannerExternalUrl);
            when(userService.getUserNameById(any())).thenReturn("username");
            when(practiceCategoryService.getPracticeCategoryNameById(any(UUID.class))).thenReturn("category");
            practiceService.changePracticeStatus(practiceId, AppUtils.PracticeStatus.ACTIVE);
            verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangePracticeStatus_Active_externalUrl() {
        try {
            practiceEntity.setPracticeBannerExternalUrl(practiceBannerExternalUrl);
            practiceEntity.setPracticeBannerStorageId(null);
            when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(practiceBannerExternalUrl);
            when(practiceCategoryService.getPracticeCategoryNameById(any(UUID.class))).thenReturn("category");
            when(userService.getUserNameById(any())).thenReturn("username");
            practiceService.changePracticeStatus(practiceId, AppUtils.PracticeStatus.ACTIVE);
            verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangePracticeStatus_InActive() {
        when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
        when(practiceRepository.save(any(PracticeEntity.class))).thenReturn(practiceEntity);
        practiceService.changePracticeStatus(practiceId, AppUtils.PracticeStatus.INACTIVE);
        verify(practiceRepository, times(1)).save(any(PracticeEntity.class));
    }

    @Test
    void testUpdatePracticeUser_Success() {
        when(practiceUserRepository.getByPracticeUserByPracticeIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(practiceUserEntity);
        when(practiceUserRepository.save(any(PracticeUserEntity.class))).thenReturn(practiceUserEntity);
        PracticeUserEntity practiceUser = practiceService.updatePracticeUser(practiceUserRequestDto);
        verify(practiceUserRepository, times(1)).save(any(PracticeUserEntity.class));
        assertNotNull(practiceUser);
    }

    @Test
    void testUpdatePracticeUser_CreateSuccess() {
        when(practiceUserRepository.getByPracticeUserByPracticeIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(null);
        when(practiceUserRepository.save(any(PracticeUserEntity.class))).thenReturn(practiceUserEntity);
        PracticeUserEntity practiceUser = practiceService.updatePracticeUser(practiceUserRequestDto);
        verify(practiceUserRepository, times(1)).save(any(PracticeUserEntity.class));
        assertNotNull(practiceUser);
    }

    @Test
    void testChangePracticeUserStatus() {
        when(practiceUserRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(practiceUserEntity));
        practiceService.changePracticeUserStatus(practiceId, AppUtils.PracticeUserStatus.IN_PROGRESS);
        verify(practiceUserRepository, times(1)).save(any(PracticeUserEntity.class));
    }

    @Test
    void testChangeRatingAndComment_RatedTrue() {
        practiceUserEntity.setRating(null);
        when(practiceUserRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(practiceUserEntity));
        when(practiceUserRepository.findNonZeroRatingsByPracticeId(any(UUID.class))).thenReturn(List.of(5F));
        when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
        practiceService.changeRatingAndComment(practiceUserId, PracticeUserRatingUpdateDto.builder().comment("comment").rating(5F).build());
        verify(practiceUserRepository, times(1)).save(any(PracticeUserEntity.class));
    }

    @Test
    void testChangeRatingAndComment_RatedFalse() {
        when(practiceUserRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(practiceUserEntity));
        when(practiceUserRepository.findNonZeroRatingsByPracticeId(any(UUID.class))).thenReturn(List.of(5F));
        when(practiceRepository.findById(any(UUID.class))).thenReturn(Optional.of(practiceEntity));
        practiceService.changeRatingAndComment(practiceUserId, PracticeUserRatingUpdateDto.builder().comment("comment").rating(5F).build());
        verify(practiceUserRepository, times(1)).save(any(PracticeUserEntity.class));
    }

    @Test
    void testGetTotalPractices_KeycloakUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(practiceRepository.count()).thenReturn(1L);
            Long count = practiceService.getTotalPractices();
            verify(practiceRepository, times(1)).count();
            assertEquals(1L, count);
        }
    }

    @Test
    void testGetTotalPractices_PortalUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(practiceRepository.countByOrgId(any(UUID.class))).thenReturn(1L);
            Long count = practiceService.getTotalPractices();
            verify(practiceRepository, times(1)).countByOrgId(any(UUID.class));
            assertEquals(1L, count);
        }
    }

    @Test
    void testGetTotalPractices_MobileUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            AppException ex = assertThrows(AppException.class, () ->
                    practiceService.getTotalPractices());
            assertEquals(AppUtils.Messages.ACCESS_DENIED.getMessage(), ex.getMessage());
        }
    }

    @Test
    void testGetTop3Practices() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(practiceRepository.getTop3ByOrgIdAndPracticeStatusOrderByCreatedAtDesc(any(UUID.class),
                    any(AppUtils.PracticeStatus.class))).thenReturn(List.of(practiceEntity));
            List<PracticeEntity> practices = practiceService.getTop3Practices();
            verify(practiceRepository, times(1)).getTop3ByOrgIdAndPracticeStatusOrderByCreatedAtDesc(any(UUID.class),
                    any(AppUtils.PracticeStatus.class));
            assert practices != null;
            assert practices.size() == 1;
        }
    }

    @Test
    void testToPracticeResponseDto() {
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(practiceStorageId, "https://test.test/file"));
        when(userService.getUserData(anyList())).thenReturn(Map.of(practiceId, "username"));
        when(organizationService.getOrgNamesByIds(anyList())).thenReturn(Map.of(orgId, "orgName"));
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(anyList())).thenReturn(Map.of(orgId, "https://test.test/icon"));
        when(practiceCategoryService.getCategoryNameIdByIds(anyList())).thenReturn(Map.of(practiceCategoryId, "categoryName"));
        List<PracticeResponseDto> practiceResponseDtos = practiceService.toPracticeResponseDto(List.of(practiceEntity));
        assertNotNull(practiceResponseDtos);
        assert practiceResponseDtos.size() == 1;
    }

    @Test
    void testToPracticeMobileResponseDto() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserId).thenReturn(orgId);
            mockedAppUtils.when(() -> AppUtils.map(any(PracticeEntity.class), eq(PracticeMobileResponseDto.class)))
                    .thenReturn(
                            PracticeMobileResponseDto.builder()
                                    .practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                                    .practiceId(practiceId).practiceUserId(practiceUserId).practiceUserStatus(practiceUserEntity.getPracticeUserStatus())
                                    .duration(1000L).tags(Set.of("tag1", "tag2")).rating(5L).ratingCount(1L)
                                    .build()
                    );
            when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(practiceStorageId, "https://test.test/file"));
            when(practiceCategoryService.getCategoryNameIdByIds(anyList())).thenReturn(Map.of(practiceCategoryId, "categoryName"));
            when(practiceUserRepository.findByPracticeIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.ofNullable(practiceUserEntity));
            List<PracticeMobileResponseDto> practiceMobileResponseDtos = practiceService.toPracticeMobileResponseDto(List.of(practiceEntity));
            assertNotNull(practiceMobileResponseDtos);
            assert practiceMobileResponseDtos.size() == 1;
        }
    }

    @Test
    void testToPracticeMobileResponseDto_NoPracticeUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserId).thenReturn(orgId);
            mockedAppUtils.when(() -> AppUtils.map(any(PracticeEntity.class), eq(PracticeMobileResponseDto.class)))
                    .thenReturn(
                            PracticeMobileResponseDto.builder()
                                    .practiceName(practiceEntity.getPracticeName()).practiceDescription(practiceEntity.getPracticeDescription())
                                    .practiceId(practiceId).practiceUserId(practiceUserId).practiceUserStatus(practiceUserEntity.getPracticeUserStatus())
                                    .duration(1000L).tags(Set.of("tag1", "tag2")).rating(5L).ratingCount(1L)
                                    .build()
                    );
            when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(practiceStorageId, "https://test.test/file"));
            when(practiceCategoryService.getCategoryNameIdByIds(anyList())).thenReturn(Map.of(practiceCategoryId, "categoryName"));
            when(practiceUserRepository.findByPracticeIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
            List<PracticeMobileResponseDto> practiceMobileResponseDtos = practiceService.toPracticeMobileResponseDto(List.of(practiceEntity));
            assertNotNull(practiceMobileResponseDtos);
            assert practiceMobileResponseDtos.size() == 1;
        }
    }
}
