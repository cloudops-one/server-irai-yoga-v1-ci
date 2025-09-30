package yoga.irai.server.organization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.*;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.storage.StorageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private StorageService storageService;
    @Mock
    private OrganizationService organizationService;
    @InjectMocks
    private OrganizationController organizationController;
    private OrganizationRequestDto organizationRequestDto;

    private OrganizationEntity organizationEntity;
    private OrganizationDropdownDto organizationDropdownDto;
    private UUID orgId;
    @BeforeEach
    void setup(){
        orgId = UUID.randomUUID();
        UUID orgIconStorageId =  UUID.randomUUID();

        organizationEntity = OrganizationEntity.builder()
                .orgId(orgId)
                .orgIconStorageId(orgIconStorageId)
                .orgStatus(AppUtils.OrganizationStatus.ACTIVE)
                .orgName("Test")
                .orgRegistrationNumber("45115")
                .orgEmail("hilton.p@terv.pro")
                .orgDescription("yoga")
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
                .bankIdentifierCode("IOBA")
                .bankBranch("Ramapuram")
                .bankAddress(UUID.randomUUID().toString())
                .bankCurrency(UUID.randomUUID().toString())
                .taxIdentificationNumber(UUID.randomUUID().toString())
                .permanentAccountNumber(UUID.randomUUID().toString())
                .goodsServicesTaxNumber(UUID.randomUUID().toString())
                .build();
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
                .bankIdentifierCode("IOBA")
                .bankBranch("Ramapuram")
                .bankAddress(UUID.randomUUID().toString())
                .bankCurrency(UUID.randomUUID().toString())
                .taxIdentificationNumber(UUID.randomUUID().toString())
                .permanentAccountNumber(UUID.randomUUID().toString())
                .goodsServicesTaxNumber(UUID.randomUUID().toString())
                .build();
    }
    @Test
    void addOrganizationTest(){
        when(organizationService.addOrganization(organizationRequestDto)).thenReturn(organizationEntity);
        ResponseEntity<AppResponseDto<OrganizationResponseDto>> response = organizationController.addOrganization(organizationRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void updateOrganizationTest(){
        when(organizationService.updateOrganization(orgId, organizationRequestDto)).thenReturn(organizationEntity);
        ResponseEntity<AppResponseDto<OrganizationResponseDto>> response = organizationController.updateOrganization(orgId, organizationRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void getOrganizationById(){
        when(organizationService.getOrganizationById(orgId)).thenReturn(organizationEntity);
        ResponseEntity<AppResponseDto<OrganizationResponseDto>> response = organizationController.getOrganizationById(orgId);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void updateOrganizationStatusTest(){
    doNothing().when(organizationService).updateOrganizationStatus(orgId, AppUtils.OrganizationStatus.ACTIVE);
        ResponseEntity<AppResponseDto<Void>> response = organizationController.updateOrganizationStatus(orgId , AppUtils.OrganizationStatus.ACTIVE);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void getPortalDashboardTest(){
        ResponseEntity<AppResponseDto<TotalDto>> response = organizationController.getPortalDashboard();
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void getOrganizationsTest(){
        int pageNumber = 2 ;
        int pageSize = 3;
        String sortBy = "id";
        Sort.Direction direction = Sort.Direction.DESC;
        String keyword = "test";

        List<OrganizationEntity> orgList = List.of(organizationEntity);
        Page<OrganizationEntity> organizationEntityPage = new PageImpl<>(orgList);

        when(organizationService.getOrganizations(pageNumber, pageSize, sortBy,
                direction, keyword)).thenReturn(organizationEntityPage);

        Map<UUID, String> userNamesByIds = new HashMap<>();
        userNamesByIds.put(organizationEntity.getCreatedBy(), "Hilton");
        userNamesByIds.put(organizationEntity.getUpdatedBy(), "Paul");
        when(userService.getUserNamesByIds(anyList())).thenReturn(userNamesByIds);

        Map<UUID, String> orgIconStorageUrlByIds = new HashMap<>();
        orgIconStorageUrlByIds.put(organizationEntity.getOrgIconStorageId(), "https://www.google.com");
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(orgIconStorageUrlByIds);

        ResponseEntity<AppResponseDto<List<OrganizationResponseDto>>> response =
                organizationController.getOrganizations(pageNumber, pageSize, sortBy, direction, keyword);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
    }
    @Test
    void getOrganizationDropdownTest(){
        int pageNumber = 2 ;
        int pageSize = 3;
        String sortBy = "id";
        Sort.Direction direction = Sort.Direction.DESC;
        String keyword = "test";
        OrganizationDropdownDto dto1 = mock(OrganizationDropdownDto.class);

        OrganizationDropdownDto dto2 = mock(OrganizationDropdownDto.class);
        
        List<OrganizationDropdownDto> orgList = List.of(dto1, dto2);
        Page<OrganizationDropdownDto> organizationEntityPage = new PageImpl<>(orgList);

        when(organizationService
                .getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword))
                .thenReturn(organizationEntityPage);
        ResponseEntity<AppResponseDto<List<OrganizationDropdownDto>>> response =
                organizationController.getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
    }

}
