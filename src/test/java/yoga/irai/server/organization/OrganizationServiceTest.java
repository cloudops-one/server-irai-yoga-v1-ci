package yoga.irai.server.organization;

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
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.storage.StorageService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AppProperties appProperties;
    @Mock
    private StorageService storageService;
    @InjectMocks
    private OrganizationService organizationService;
    private OrganizationRequestDto organizationRequestDto;
    private OrganizationEntity organizationEntity;
    private UUID orgId;
    private UUID principalOrgId;
    private int pageNumber;
    private int pageSize;
    private String sortBy;
    private Sort.Direction direction;
    private String keyword;
    private List<OrganizationEntity> orgList;

    @BeforeEach
    void setup() {
        pageNumber = 0;
        pageSize = 10;
        orgId = UUID.randomUUID();
        principalOrgId = UUID.randomUUID();
        sortBy = "createdAt";
        direction = Sort.Direction.ASC;
        keyword = "false";
        orgList = List.of(
                OrganizationEntity.builder()
                        .orgId(UUID.randomUUID())
                        .orgName("test")
                        .orgStatus(AppUtils.OrganizationStatus.ACTIVE)
                        .build()
        );
        organizationRequestDto = OrganizationRequestDto.builder()
                .orgIconStorageId(orgId)
                .orgName("test")
                .orgRegistrationNumber("1234")
                .orgEmail("hilton.p@terv.pro")
                .orgDescription("test")
                .addresses(List.of(AddressDto.builder().id(0).addressLine1("2/1").addressLine2("Church street")
                        .city("Tirunelveli").stateProvince("Tamil Nadu").postalCode("627502").country("India").build()))
                .contacts(List.of(ContactDto.builder()
                        .id(0)
                        .isPrimary(Boolean.TRUE)
                        .name("Hilton")
                        .mobile("+919940798142")
                        .email("hilton.p@terv.pro").build()))
                .urls(List.of(UrlDto.builder()
                        .id(0)
                        .url("https://www.google.com")
                        .type(AppUtils.UrlType.YOUTUBE)
                        .build()))
                .bankName("Indian overseas bank")
                .bankAccountNumber("565618151564256150231")
                .bankAccountType("Savings")
                .bankIdentifierCode("IOWA")
                .bankBranch("Brahmaputra")
                .bankAddress(UUID.randomUUID().toString())
                .bankCurrency(UUID.randomUUID().toString())
                .taxIdentificationNumber(UUID.randomUUID().toString())
                .permanentAccountNumber(UUID.randomUUID().toString())
                .goodsServicesTaxNumber(UUID.randomUUID().toString())
                .build();
        organizationEntity = AppUtils.map(organizationRequestDto, OrganizationEntity.class);
        organizationEntity.setOrgId(UUID.randomUUID());
    }

    @Test
    void addOrganizationTest() {
        when(organizationRepository.existsByOrgName(organizationRequestDto.getOrgName())).thenReturn(false);
        when(organizationRepository.save(any(OrganizationEntity.class))).thenReturn(organizationEntity);
        OrganizationEntity response = organizationService.addOrganization(organizationRequestDto);
        assertNotNull(response);
        assertEquals(organizationEntity.getOrgName(), response.getOrgName());
        verify(organizationRepository, times(1)).save(any(OrganizationEntity.class));
    }

    @Test
    void addOrganization_nameExists_throwsException() {
        when(organizationRepository.existsByOrgName(organizationRequestDto.getOrgName())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> organizationService.addOrganization(organizationRequestDto));
        assertEquals("Name already exists", exception.getMessage());
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateOrganizationTest() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        organizationEntity.setOrgIconStorageId(id1);
        organizationRequestDto.setOrgIconStorageId(id2);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organizationEntity));
        when(organizationRepository.save(any(OrganizationEntity.class))).thenAnswer(i -> i.getArgument(0));
        OrganizationEntity result = organizationService.updateOrganization(orgId, organizationRequestDto);
        verify(storageService, times(1)).deleteStorageById(any(UUID.class));
        verify(organizationRepository, times(1)).save(any(OrganizationEntity.class));
        assertEquals(orgId, result.getOrgId());
        assertEquals(organizationRequestDto.getOrgName(), result.getOrgName());
        assertEquals(organizationRequestDto.getOrgIconStorageId(), result.getOrgIconStorageId());
    }
    @Test
    void updateOrganizationTest_IfConditionFails() {
        UUID id1 = UUID.randomUUID();
        organizationEntity.setOrgIconStorageId(id1);
        organizationRequestDto.setOrgIconStorageId(id1);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organizationEntity));
        when(organizationRepository.save(any(OrganizationEntity.class))).thenAnswer(i -> i.getArgument(0));
        OrganizationEntity result = organizationService.updateOrganization(orgId, organizationRequestDto);
        verify(storageService, never()).deleteStorageById(any(UUID.class));
        verify(organizationRepository, times(1)).save(any(OrganizationEntity.class));
        assertEquals(orgId, result.getOrgId());
        assertEquals(organizationRequestDto.getOrgName(), result.getOrgName());
        assertEquals(organizationRequestDto.getOrgIconStorageId(), result.getOrgIconStorageId());
    }

    @Test
    void getOrganizationsMobileUserTest() {
        Page<OrganizationEntity> pageResult = new PageImpl<>(orgList);
        String defaultOrgId = UUID.randomUUID().toString();
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(appProperties.getDefaultOrganizationId()).thenReturn(defaultOrgId);
            when(organizationRepository.search(
                    anyString(),
                    anySet(),
                    any(),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            Page<OrganizationEntity> result = organizationService.getOrganizations(pageNumber, pageSize, sortBy, direction, keyword);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationDropdownMobileUserTest() {
        String defaultOrgId = UUID.randomUUID().toString();
        OrganizationDropdownDto mockDropdown = mock(OrganizationDropdownDto.class);
        Page<OrganizationDropdownDto> pageResult = new PageImpl<>(List.of(mockDropdown));
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(appProperties.getDefaultOrganizationId()).thenReturn(defaultOrgId);
            when(organizationRepository.getDropdownSearch(
                    anyString(),
                    anySet(),
                    eq(AppUtils.OrganizationStatus.INACTIVE),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            OrganizationService service = new OrganizationService(appProperties, storageService, organizationRepository);
            Page<OrganizationDropdownDto> result = service.getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationDropdownPortalUserTest() {
        OrganizationDropdownDto mockDropdown = mock(OrganizationDropdownDto.class);
        Page<OrganizationDropdownDto> pageResult = new PageImpl<>(List.of(mockDropdown));
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(organizationRepository.getDropdownSearch(
                    anyString(),
                    anySet(),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            OrganizationService service = new OrganizationService(appProperties, storageService, organizationRepository);
            Page<OrganizationDropdownDto> result = service.getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationsPortalUserTest() {
        Page<OrganizationEntity> pageResult = new PageImpl<>(orgList);
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(organizationRepository.search(
                    anyString(),
                    anySet(),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            Page<OrganizationEntity> result =
                    organizationService.getOrganizations(pageNumber, pageSize, sortBy, direction, keyword);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationDropDownKeyCloakUserTest() {
        OrganizationDropdownDto mockDropdown = mock(OrganizationDropdownDto.class);
        Page<OrganizationDropdownDto> pageResult = new PageImpl<>(List.of(mockDropdown));
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(organizationRepository.getDropdownSearch(
                    anyString(),
                    isNull(),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            OrganizationService service = new OrganizationService(appProperties, storageService, organizationRepository);
            Page<OrganizationDropdownDto> result = service.getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationsKeycloakUserTest() {
        Page<OrganizationEntity> pageResult = new PageImpl<>(orgList);
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(principalOrgId);
            when(organizationRepository.search(
                    anyString(),
                    isNull(),
                    isNull(),
                    any(Pageable.class)
            )).thenReturn(pageResult);
            Page<OrganizationEntity> result =
                    organizationService.getOrganizations(pageNumber, pageSize, sortBy, direction, keyword);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void getOrganizationByIdTest() {
        organizationEntity.setOrgId(orgId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organizationEntity));
        OrganizationEntity result = organizationService.getOrganizationById(orgId);
        assertNotNull(result);
        assertEquals(orgId, result.getOrgId());
        verify(organizationRepository, times(1)).findById(orgId);
    }

    @Test
    void getOrgNameByOrgIdTest() {
        when(organizationRepository.findByOrgId(orgId)).thenReturn(organizationEntity);
        String result = organizationService.getOrgNameByOrgId(orgId);
        assertNotNull(result);
    }
    @Test
    void getOrgNameByOrgIdTest_Null(){
        when(organizationRepository.findByOrgId(orgId)).thenReturn(null);
        String result = organizationService.getOrgNameByOrgId(orgId);
        assertEquals("" , result);
        assertNotNull(result);
    }

    @Test
    void getOrgNamesByIdsTest() {
        UUID orgId1 = UUID.randomUUID();
        UUID orgId2 = UUID.randomUUID();
        List<UUID> orgIds = List.of(orgId1, orgId2);
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);
        when(dto1.getOrgId()).thenReturn(orgId1);
        when(dto1.getOrgName()).thenReturn("Org One");
        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        when(dto2.getOrgId()).thenReturn(orgId2);
        when(dto2.getOrgName()).thenReturn("Org Two");
        List<OrganizationDropdownDto> dtoList = List.of(dto1, dto2);
        when(organizationRepository.findOrgNameByOrgIdIn(orgIds)).thenReturn(dtoList);
        Map<UUID, String> result = organizationService.getOrgNamesByIds(orgIds);
        assertEquals(2, result.size());
        assertEquals("Org One", result.get(orgId1));
        assertEquals("Org Two", result.get(orgId2));
        verify(organizationRepository, times(1)).findOrgNameByOrgIdIn(orgIds);
    }

    @Test
    void getOrgIconStorageUrlByIdsTest() {
        UUID orgId1 = UUID.randomUUID();
        UUID orgId2 = UUID.randomUUID();
        List<UUID> orgIds = List.of(orgId1, orgId2);
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);
        when(dto1.getOrgId()).thenReturn(orgId1);
        when(dto1.getOrgIconStorageId()).thenReturn("https://bucket.com/icon1.png");
        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        when(dto2.getOrgId()).thenReturn(orgId2);
        when(dto2.getOrgIconStorageId()).thenReturn("https://bucket.com/icon2.png");
        List<OrganizationDropdownDto> dtoList = List.of(dto1, dto2);
        when(organizationRepository.findOrgIconStorageIdByOrgIdIn(orgIds)).thenReturn(dtoList);
        Map<UUID, String> result = organizationService.getOrgIconStorageUrlByIds(orgIds);
        assertEquals(2, result.size());
        assertEquals("https://bucket.com/icon1.png", result.get(orgId1));
        assertEquals("https://bucket.com/icon2.png", result.get(orgId2));
        verify(organizationRepository, times(1)).findOrgIconStorageIdByOrgIdIn(orgIds);
    }

    @Test
    void updateOrganizationStatusTest() {
        organizationEntity.setOrgId(orgId);
        organizationEntity.setOrgStatus(AppUtils.OrganizationStatus.INACTIVE);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organizationEntity));
        when(organizationRepository.save(any(OrganizationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        organizationService.updateOrganizationStatus(orgId, AppUtils.OrganizationStatus.ACTIVE);
        assertEquals(AppUtils.OrganizationStatus.ACTIVE, organizationEntity.getOrgStatus());
        verify(organizationRepository, times(1)).findById(orgId);
        verify(organizationRepository, times(1)).save(organizationEntity);
    }

    @Test
    void getOrgIconStorageIdToSignedIconUrlTest() {
        UUID iconStorageId = UUID.randomUUID();
        String signedUrl = "https://storage-service.com/" + iconStorageId;
        organizationEntity.setOrgId(orgId);
        organizationEntity.setOrgIconStorageId(iconStorageId);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organizationEntity));
        when(storageService.getStorageUrl(iconStorageId)).thenReturn(signedUrl);
        String result = organizationService.getOrgIconStorageIdToSignedIconUrl(orgId);
        assertEquals(signedUrl, result);
        verify(organizationRepository, times(1)).findById(orgId);
        verify(storageService, times(1)).getStorageUrl(iconStorageId);
    }


    @Test
    void getOrgIconStorageIdToSignedIconUrlReturnMapTest() {
        UUID orgId1 = UUID.randomUUID();
        UUID orgId2 = UUID.randomUUID();
        UUID storageId1 = UUID.randomUUID();
        UUID storageId2 = UUID.randomUUID();
        List<UUID> orgIds = List.of(orgId1, orgId2);
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);
        when(dto1.getOrgId()).thenReturn(orgId1);
        when(dto1.getOrgIconStorageId()).thenReturn(storageId1.toString());
        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        when(dto2.getOrgId()).thenReturn(orgId2);
        when(dto2.getOrgIconStorageId()).thenReturn(storageId2.toString());
        List<OrganizationDropdownDto> dtoList = List.of(dto1, dto2);
        when(organizationRepository.findOrgIconStorageIdByOrgIdIn(orgIds)).thenReturn(dtoList);
        Map<UUID, String> signedUrls = Map.of(
                storageId1, "https://signed-url.com/icon1.png",
                storageId2, "https://signed-url.com/icon2.png"
        );
        when(storageService.getSignedStorageUrlByIds(anyList()))
                .thenReturn(signedUrls);
        Map<UUID, String> result = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("https://signed-url.com/icon1.png", result.get(orgId1));
        assertEquals("https://signed-url.com/icon2.png", result.get(orgId2));
        verify(organizationRepository, times(2)).findOrgIconStorageIdByOrgIdIn(orgIds);
    }

    @Test
    void getOrgIconStorageIdToSignedIconUrl_Null() {
        UUID orgId1 = UUID.randomUUID();
        UUID orgId2 = UUID.randomUUID();
        UUID storageId1 = UUID.randomUUID();
        UUID storageId2 = UUID.randomUUID();
        List<UUID> orgIds = List.of(orgId1, orgId2);
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);
        when(dto1.getOrgId()).thenReturn(orgId1);
        when(dto1.getOrgIconStorageId()).thenReturn(storageId1.toString());
        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        when(dto2.getOrgId()).thenReturn(orgId2);
        when(dto2.getOrgIconStorageId()).thenReturn(storageId2.toString());
        List<OrganizationDropdownDto> dtoList = List.of(dto1, dto2);
        when(organizationRepository.findOrgIconStorageIdByOrgIdIn(orgIds)).thenReturn(dtoList);
        when(storageService.getSignedStorageUrlByIds(anyList()))
                .thenReturn(null);
        Map<UUID, String> result = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getOrgIconStorageIdToSignedIconUrl_IfConditionTriggered() {
        UUID orgId1 = UUID.randomUUID();
        UUID orgId2 = UUID.randomUUID();
        UUID storageId1 = UUID.randomUUID();
        UUID storageId2 = UUID.randomUUID();
        List<UUID> orgIds = List.of(orgId1, orgId2);
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);
        when(dto1.getOrgId()).thenReturn(orgId1);
        when(dto1.getOrgIconStorageId()).thenReturn(storageId1.toString());
        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        when(dto2.getOrgId()).thenReturn(orgId2);
        when(dto2.getOrgIconStorageId()).thenReturn(storageId2.toString());
        List<OrganizationDropdownDto> dtoList = List.of(dto1, dto2);
        when(organizationRepository.findOrgIconStorageIdByOrgIdIn(orgIds)).thenReturn(dtoList);
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Collections.emptyMap());
        Map<UUID, String> result = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty map because signedIconUrls was empty");
        verify(organizationRepository, times(1)).findOrgIconStorageIdByOrgIdIn(orgIds);
        verify(storageService, times(1)).getSignedStorageUrlByIds(anyList());
    }

    @Test
    void getTopicNameTest() {
        String orgName = "My Test Org";
        try (MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalOrgId).thenReturn(orgId);
            when(organizationRepository.findByOrgId(orgId)).thenReturn(
                    OrganizationEntity.builder().orgId(orgId).orgName(orgName).build()
            );
            String result = organizationService.getTopicName();
            assertEquals("my-test-org", result);
            verify(organizationRepository, times(1)).findByOrgId(orgId);
        }
    }
    @Test
    void getTotalOrganizationsKeycloakUserTest(){
        try(MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(organizationRepository.count()).thenReturn(1L);
            Long result = organizationService.getTotalOrganizations();
            assertEquals(1L, result);
            verify(organizationRepository, times(1)).count();
        }
    }
    @Test
    void getTotalOrganizationsPortalUserTest(){
        try(MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            when(AppUtils.getPrincipalOrgId()).thenReturn(orgId);
            when(organizationRepository.countByOrgId(orgId)).thenReturn(1L);
            Long result = organizationService.getTotalOrganizations();
            assertEquals(1L, result);
            verify(organizationRepository, times(1)).countByOrgId(orgId);
        }
    }
    @Test
    void getTotalOrganizationsMobileUserTest(){
        try(MockedStatic<AppUtils> utilsMock = mockStatic(AppUtils.class)) {
            utilsMock.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            AppException  exception = assertThrows(AppException.class, () ->
                    organizationService.getTotalOrganizations());
            assertEquals(AppUtils.Messages.ACCESS_DENIED.getMessage(), exception.getMessage());
            verifyNoInteractions(organizationRepository);
        }
    }
}

