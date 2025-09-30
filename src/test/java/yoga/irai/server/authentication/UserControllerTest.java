package yoga.irai.server.authentication;

import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.controller.UserController;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.storage.StorageService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private UserEntity userEntity;
    @Mock
    private StorageService storageService;
    @Mock
    private OrganizationService organizationService;
    @InjectMocks
    private UserController userController;
    private UUID userId;
    private UserCreationRequestDto userCreationRequestDto;
    private UserRequestDto userRequestDto;
    private UpdatePasswordDto updatePasswordDto;

    @BeforeEach
    void setup() throws ParseException {
        userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID userIconStorageId = UUID.randomUUID();
        updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword("Hil@123")
                .newPassword("Hilton@14")
                .build();
        userRequestDto = UserRequestDto.builder()
                .orgId(orgId)
                .userIconStorageId(userIconStorageId)
                .userFirstName("Hilton")
                .userLastName("Paul")
                .userEmail("hilton.p@terv.pro")
                .userMobile("+919940798142")
                .gender(AppUtils.Gender.MALE)
                .dateOfBirth(new SimpleDateFormat("dd-MM-yyyy").parse("09-06-2003"))
                .bloodGroup(AppUtils.BloodGroup.A_POSITIVE)
                .addresses(List.of(AddressDto.builder().id(0).addressLine1("2/1").addressLine2("Church street")
                        .city("Tirunelveli").stateProvince("Tamil Nadu").postalCode("627502").country("India")
                        .build())).build();
        userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setPassword("Hil@123");
        userCreationRequestDto.setOrgId(orgId);
        userCreationRequestDto.setUserIconStorageId(userIconStorageId);
        userCreationRequestDto.setUserFirstName("Hilton");
        userCreationRequestDto.setUserLastName("Paul");
        userCreationRequestDto.setUserEmail("hilton.p@terv.pro");
        userCreationRequestDto.setUserMobile("+919940798142");
        userCreationRequestDto.setGender(AppUtils.Gender.MALE);
        userCreationRequestDto.setDateOfBirth(new SimpleDateFormat("dd/MM/yyyy").parse("09/06/2003"));
        userCreationRequestDto.setBloodGroup(AppUtils.BloodGroup.A_POSITIVE);
        userCreationRequestDto.setAddresses(List.of(AddressDto.builder().id(0).addressLine1("2/1").addressLine2("Church street")
                .city("Tirunelveli").stateProvince("Tamil Nadu").postalCode("627502").country("India").build()));

    }
    @Test
    void addUserTest(){
        when(userService.addUser(userCreationRequestDto)).thenReturn(userEntity);
        when(userService.getUserDto(userEntity)).thenReturn(new UserResponseDto());
        ResponseEntity<AppResponseDto<UserResponseDto>> response = userController.addUser(userCreationRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void updateUserTest(){
        when(userService.updateUser(userId , userRequestDto)).thenReturn(userEntity);
        when(userService.getUserDto(userEntity)).thenReturn(new UserResponseDto());
        ResponseEntity<AppResponseDto<UserResponseDto>> response = userController.updateUser(userId , userRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;

    }
    @Test
    void getUserByIdTest(){
        when(userService.getUserById(userId)).thenReturn(userEntity);
        when(userService.getUserDto(userEntity)).thenReturn(new UserResponseDto());
        ResponseEntity<AppResponseDto<UserResponseDto>> response = userController.getUserById(userId);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void getAllUsersTest() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID iconId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setCreatedBy(userId);
        user.setUpdatedBy(userId);
        user.setOrgId(orgId);
        user.setUserIconStorageId(iconId);

        List<UserEntity> users = List.of(user);
        Page<UserEntity> mockPage = new PageImpl<>(users);

        when(userService.getUsers(1, 10, "", Sort.Direction.DESC, "")).thenReturn(mockPage);
        when(userService.getUserNamesByIds(List.of(userId))).thenReturn(Map.of(userId, "Hilton"));
        when(organizationService.getOrgNamesByIds(List.of(orgId))).thenReturn(Map.of(orgId, "Irai Org"));
        when(storageService.getSignedStorageUrlByIds(List.of(iconId))).thenReturn(Map.of(iconId, "https://mocked.url/icon.png"));

        AppResponseDto.Pageable pageable = AppResponseDto.Pageable.builder()
                .pageNumber(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1L)
                .sortBy("")
                .sortDirection("DESC")
                .build();

        try (
                MockedStatic<AppUtils> utils = Mockito.mockStatic(AppUtils.class);
                MockedStatic<AppResponseDto> responseDtoStatic = Mockito.mockStatic(AppResponseDto.class, CALLS_REAL_METHODS)
        ) {
            UserResponseDto dto = new UserResponseDto();
            dto.setCreatedBy(userId);
            dto.setUpdatedBy(userId);
            dto.setOrgId(orgId);
            dto.setUserIconStorageId(iconId);
            dto.setCreatedByName("Hilton");

            utils.when(() -> AppUtils.map(any(UserEntity.class), eq(UserResponseDto.class))).thenReturn(dto);
            responseDtoStatic.when(() -> AppResponseDto.buildPageable(mockPage, "", Sort.Direction.DESC)).thenReturn(pageable);

            ResponseEntity<AppResponseDto<List<UserResponseDto>>> response =
                    userController.getAllUsers(1, 10, "", Sort.Direction.DESC, "");

            assertAll(
                    () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                    () -> assertNotNull(response.getBody()),
                    () -> {
                        assertNotNull(response.getBody());
                        assertEquals("Hilton", response.getBody().getData().getFirst().getCreatedByName());
                    },
                    () -> {
                        assertNotNull(response.getBody());
                        assertEquals(1L, response.getBody().getPageable().getTotalElements());
                    }
            );
        }
    }
    @Test
    void deleteUserTest(){
        doNothing().when(userService).updateUserStatus(userId, AppUtils.UserStatus.DELETED);
        ResponseEntity<AppResponseDto<Void>> response = userController.deleteUser(userId);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void updateUserStatusTest(){
        doNothing().when(userService).updateUserStatus(userId, AppUtils.UserStatus.INACTIVE);
        ResponseEntity<AppResponseDto<Void>> response = userController.updateUserStatus(userId , AppUtils.UserStatus.INACTIVE);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void updateUserStatusAioTest(){
        when(userService.getUserById(userId)).thenReturn(userEntity);
        List<UserAoiDto> mockAoiList = List.of(new UserAoiDto());
        when(userService.getUserAoi(userEntity)).thenReturn(mockAoiList);
        ResponseEntity<AppResponseDto<List<UserAoiDto>>> response = userController.updateUserStatus(userId);
        assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.UPDATE_SUCCESS.getMessage(userId),
                response.getBody().getMessage());
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void getUserIconStorageUrlTest() {
        try (MockedStatic<AppUtils> mockedStatic = Mockito.mockStatic(AppUtils.class)) {
            mockedStatic.when(AppUtils::getPrincipalUserId).thenReturn(userId);
            when(userService.getUserById(userId)).thenReturn(userEntity);
            when(userEntity.getUserIconStorageId()).thenReturn(userId);
            when(storageService.getStorageUrl(userId)).thenReturn("https://mocked.url/icon.png");
            ResponseEntity<AppResponseDto<UserIconStorageUrlDto>> response = userController.getUserIconStorageUrl();
            assert response.getStatusCode() == HttpStatus.OK;
            assertNotNull(response.getBody());
            assertEquals("https://mocked.url/icon.png", response.getBody().getData().getUserIconStorageUrl());
        }
    }
    @Test
    void resetUserPasswordTest(){
        doNothing().when(userService).resetUserPassword(userId, updatePasswordDto);
        ResponseEntity<AppResponseDto<Void>> response = userController.resetUserPassword(userId, updatePasswordDto);
        assert response.getStatusCode() == HttpStatus.OK;

    }
    @Test
    void dashboardTest(){
        when(userService.getUserStats()).thenReturn(new UserStatsDto());
        ResponseEntity<AppResponseDto<UserStatsDto>> response = userController.dashboard();
        assert response.getStatusCode() == HttpStatus.OK;
    }
}
