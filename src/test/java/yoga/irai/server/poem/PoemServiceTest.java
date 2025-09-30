package yoga.irai.server.poem;

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
import yoga.irai.server.mobile.dto.PoemMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoemServiceTest {

    @Mock
    private PoemRepository poemRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private UserService userService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SettingService settingService;

    @InjectMocks
    private PoemService poemService;

    private UUID poemId;
    private UUID updatedPoemIconStorageId;
    private UUID updatedPoemBannerStorageId;
    private UUID updatedPoemStorageId;
    private UUID poemIconStorageId;
    private UUID poemBannerStorageId;
    private UUID poemStorageId;
    private UUID orgId;
    private PoemEntity poemEntity;
    private PoemRequestDto poemRequestDto;
    private String poemBannerExternalUrl;
    private String updatedPoemStorageExternalUrl;
    private String updatedPoemIconExternalUrl;
    private String updatedPoemBannerExternalUrl;
    private Page<PoemEntity> mockPage;

    @BeforeEach
    void setUp() {
        poemId = UUID.randomUUID();
        poemStorageId = UUID.randomUUID();
        poemIconStorageId = UUID.randomUUID();
        poemBannerStorageId = UUID.randomUUID();
        poemBannerExternalUrl = "http://test.test/banner";
        updatedPoemStorageId = UUID.randomUUID();
        updatedPoemIconStorageId = UUID.randomUUID();
        updatedPoemBannerStorageId = UUID.randomUUID();
        updatedPoemStorageExternalUrl = "http://test.test/file";
        updatedPoemIconExternalUrl = "http://test.test/icon";
        updatedPoemBannerExternalUrl = "http://test.test/banner";

        orgId = UUID.randomUUID();
        poemRequestDto = PoemRequestDto.builder()
                .poemName("Test Poem").poemDescription("This is a test poem.")
                .orgId(orgId).poemAuthor("Author").poemDuration(1000L).poemStorageId(poemStorageId)
                .poemBannerStorageId(poemBannerStorageId).poemIconStorageId(poemIconStorageId)
                .poemText("This is a test poem.").poemTags(Set.of("tag1", "tag2"))
                .build();
        poemEntity = PoemEntity.builder()
                .poemId(poemId).poemName("Test Poem").poemDescription("This is a test poem.").orgId(orgId)
                .poemAuthor("Author").poemDuration(1000L).poemStorageId(poemStorageId)
                .poemBannerStorageId(poemBannerStorageId).poemIconStorageId(poemIconStorageId)
                .poemText("This is a test poem.").poemTags("[\"tag1\",\"tag2\"]").poemStatus(AppUtils.PoemStatus.ACTIVE)
                .build();
        poemEntity.setCreatedBy(UUID.randomUUID());
        poemEntity.setUpdatedBy(UUID.randomUUID());
        mockPage = new PageImpl<>(List.of(poemEntity));
    }

    @Test
    void testAddPoem_Success() {
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.existsByPoemName(poemRequestDto.getPoemName()))
                .thenReturn(false);
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.addPoem(poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        verify(poemRepository, times(1)).existsByPoemName(anyString());
        assert poem != null;
        assert poem.getPoemId().equals(poemId);
    }

    @Test
    void testAddPoem_Success_NoTags() {
        poemRequestDto.setPoemTags(null);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.existsByPoemName(poemRequestDto.getPoemName()))
                .thenReturn(false);
        PoemEntity poem = poemService.addPoem(poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        verify(poemRepository, times(1)).existsByPoemName(anyString());
        assert poem != null;
        assert poem.getPoemId().equals(poemId);
    }

    @Test
    void testAddPoem_Failure() {
        when(poemRepository.existsByPoemName(poemRequestDto.getPoemName()))
                .thenReturn(true);
        AppException ex = assertThrows(AppException.class, () ->
                poemService.addPoem(poemRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(poemRepository, never()).save(any(PoemEntity.class));
    }

    @Test
    void testUpdatePoem_Success() {
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemId().equals(poemId);
    }

    @Test
    void testUpdatePoem_Success_NoTags() {
        poemRequestDto.setPoemTags(null);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemId().equals(poemId);
    }

    @Test
    void testUpdatePoem_Success_IconStorageId_NoChange() {
        poemRequestDto.setPoemIconStorageId(poemIconStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemIconStorageId().equals(poemIconStorageId);
    }

    @Test
    void testUpdatePoem_Success_BannerStorageId_NoChange() {
        poemRequestDto.setPoemBannerStorageId(poemBannerStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemBannerStorageId().equals(poemBannerStorageId);
    }

    @Test
    void testUpdatePoem_Success_StorageId_NoChange() {
        poemRequestDto.setPoemStorageId(poemStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemStorageId().equals(poemStorageId);
    }

    @Test
    void testUpdatePoem_Success_IconStorageId() {
        poemRequestDto.setPoemIconStorageId(updatedPoemIconStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemIconStorageId().equals(updatedPoemIconStorageId);
    }

    @Test
    void testUpdatePoem_Success_BannerStorageId() {
        poemRequestDto.setPoemBannerStorageId(updatedPoemBannerStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemBannerStorageId().equals(updatedPoemBannerStorageId);
    }

    @Test
    void testUpdatePoem_Success_StorageId() {
        poemRequestDto.setPoemStorageId(updatedPoemStorageId);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);
        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemStorageId().equals(updatedPoemStorageId);
    }

    @Test
    void testUpdatePoem_Success_IconStorageUrl_NoChange() {
        poemRequestDto.setPoemIconExternalUrl(updatedPoemIconExternalUrl);
        poemRequestDto.setPoemIconStorageId(null);
        poemEntity.setPoemIconExternalUrl(updatedPoemIconExternalUrl);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemIconExternalUrl().equals(updatedPoemIconExternalUrl);
    }

    @Test
    void testUpdatePoem_Success_BannerStorageUrl_NoChange() {
        poemRequestDto.setPoemBannerExternalUrl(updatedPoemBannerExternalUrl);
        poemRequestDto.setPoemBannerStorageId(null);
        poemEntity.setPoemBannerExternalUrl(updatedPoemBannerExternalUrl);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemBannerExternalUrl().equals(updatedPoemBannerExternalUrl);
    }

    @Test
    void testUpdatePoem_Success_StorageUrl_NoChange() {
        poemRequestDto.setPoemExternalUrl(updatedPoemStorageExternalUrl);
        poemRequestDto.setPoemStorageId(null);
        poemEntity.setPoemExternalUrl(updatedPoemStorageExternalUrl);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemExternalUrl().equals(updatedPoemStorageExternalUrl);
    }

    @Test
    void testUpdatePoem_Success_IconStorageUrl() {
        poemRequestDto.setPoemIconExternalUrl(updatedPoemIconExternalUrl);
        poemRequestDto.setPoemIconStorageId(null);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemIconExternalUrl().equals(updatedPoemIconExternalUrl);
    }

    @Test
    void testUpdatePoem_Success_BannerStorageUrl() {
        poemRequestDto.setPoemBannerExternalUrl(updatedPoemBannerExternalUrl);
        poemRequestDto.setPoemBannerStorageId(null);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemBannerExternalUrl().equals(updatedPoemBannerExternalUrl);
    }

    @Test
    void testUpdatePoem_Success_StorageUrl() {
        poemRequestDto.setPoemExternalUrl(updatedPoemStorageExternalUrl);
        poemRequestDto.setPoemStorageId(null);
        when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        doNothing().when(settingService).updateSetting(any(AppUtils.SettingName.class), anySet());
        PoemEntity poem = poemService.updatePoem(poemId, poemRequestDto);

        verify(poemRepository, times(1)).save(any(PoemEntity.class));
        assert poem != null;
        assert poem.getPoemExternalUrl().equals(updatedPoemStorageExternalUrl);
    }

    @Test
    void testGetPoems_MobileUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            when(poemRepository.search(anyString(), anySet(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            when(organizationService.getOrgIdsForMobile()).thenReturn(Set.of(orgId));
            Page<PoemEntity> poems = poemService.getPoems(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            verify(poemRepository, times(1)).search(anyString(), anySet(), any(), any(Pageable.class));
            assert poems != null;
            assert !poems.getContent().isEmpty();
        }
    }

    @Test
    void testGetPoems_PortalUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            when(poemRepository.search(anyString(), anySet(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<PoemEntity> poems = poemService.getPoems(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            assertNotNull(poems);
            assertNotNull(poems.getContent());
        }
    }

    @Test
    void testGetPoems_AdminUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(poemRepository.search(anyString(), any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<PoemEntity> poems = poemService.getPoems(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            assertNotNull(poems);
            assertNotNull(poems.getContent());
        }
    }

    @Test
    void testDeletePoems(){
        poemEntity.setPoemBannerStorageId(poemBannerStorageId);
        poemEntity.setPoemIconStorageId(poemIconStorageId);
        poemEntity.setPoemStorageId(poemStorageId);
        when(poemRepository.findById(any(UUID.class))).thenReturn(Optional.of(poemEntity));
        doNothing().when(poemRepository).deleteById(any(UUID.class));
        doNothing().when(storageService).deleteStorageByIds(anySet());
        poemService.deletePoem(poemId);
        verify(poemRepository, times(1)).deleteById(any(UUID.class));
        verify(poemRepository).findById(any(UUID.class));
    }

    @Test
    void testDeletePoems_NoStorageId(){
        poemEntity.setPoemBannerStorageId(null);
        poemEntity.setPoemIconStorageId(null);
        poemEntity.setPoemStorageId(null);
        when(poemRepository.findById(any(UUID.class))).thenReturn(Optional.of(poemEntity));
        doNothing().when(poemRepository).deleteById(any(UUID.class));
        poemService.deletePoem(poemId);
        verify(poemRepository, times(1)).deleteById(any(UUID.class));
        verify(poemRepository).findById(any(UUID.class));
    }

    @Test
    void testChangePoemStatus_Active_storageUrl(){
        try {
            poemEntity.setPoemBannerExternalUrl(null);
            poemEntity.setPoemBannerStorageId(poemBannerStorageId);
            when(poemRepository.findById(any(UUID.class))).thenReturn(Optional.of(poemEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(poemBannerExternalUrl);
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            poemService.changePoemStatus(poemId, AppUtils.PoemStatus.ACTIVE);
            verify(poemRepository, times(1)).save(any(PoemEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangePoemStatus_Active_externalUrl(){
        try {
            poemEntity.setPoemBannerExternalUrl(poemBannerExternalUrl);
            poemEntity.setPoemBannerStorageId(null);
            when(poemRepository.findById(any(UUID.class))).thenReturn(Optional.of(poemEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(poemBannerExternalUrl);
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            poemService.changePoemStatus(poemId, AppUtils.PoemStatus.ACTIVE);
            verify(poemRepository, times(1)).save(any(PoemEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangePoemStatus_InActive(){
        try {
            when(poemRepository.findById(any(UUID.class))).thenReturn(Optional.of(poemEntity));
            when(poemRepository.save(any(PoemEntity.class))).thenReturn(poemEntity);
            poemService.changePoemStatus(poemId, AppUtils.PoemStatus.INACTIVE);
            verify(poemRepository, times(1)).save(any(PoemEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetTopPoems(){
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(poemRepository.getTop3ByOrgIdAndPoemStatusOrderByCreatedAtDesc(any(UUID.class), any(AppUtils.PoemStatus.class))).thenReturn(List.of(poemEntity));
            List<PoemEntity> poems = poemService.getTop3Poems();
            verify(poemRepository, times(1)).getTop3ByOrgIdAndPoemStatusOrderByCreatedAtDesc(any(UUID.class), any(AppUtils.PoemStatus.class));
            assertNotNull(poems);
            assert !poems.isEmpty();
        }
    }

    @Test
    void testToPoemResponseDto(){
        UUID storageId = UUID.randomUUID();
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(storageId,"https://storage.com/url"));
        List<PoemResponseDto> poemResponseDtoList = poemService.toPoemResponseDto(List.of(poemEntity));
        assertNotNull(poemResponseDtoList);
    }

    @Test
    void testToPoemMobileResponseDto(){
        UUID storageId = UUID.randomUUID();
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(storageId,"https://storage.com/url"));
        List<PoemMobileResponseDto> poemMobileResponseDtoList = poemService.toPoemMobileResponseDto(List.of(poemEntity));
        assertNotNull(poemMobileResponseDtoList);
    }

    @Test
    void testGetTotalPoems_PortalUser(){
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(poemRepository.countByOrgId(any(UUID.class))).thenReturn(1L);
            Long count  = poemService.getTotalPoems();
            verify(poemRepository, times(1)).countByOrgId(any(UUID.class));
            assertEquals(1L, count);
        }
    }

    @Test
    void testGetTotalPoems_KeycloakUser(){
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(poemRepository.count()).thenReturn(1L);
            Long count  = poemService.getTotalPoems();
            verify(poemRepository, times(1)).count();
            assertEquals(1L, count);
        }
    }

    @Test
    void testGetTotalPoems_MobileUser(){
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);

            AppException exception = assertThrows(AppException.class, ()->poemService.getTotalPoems());
            assertNotNull(exception);
        }
    }

    @Test
    void testUpdateViewCount(){
        when(poemRepository.findById(poemId)).thenReturn(Optional.of(poemEntity));
        poemService.updateViewCount(poemId);
        verify(poemRepository, times(1)).save(poemEntity);
    }
}
