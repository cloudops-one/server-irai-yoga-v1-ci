package yoga.irai.server.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;
import yoga.irai.server.authentication.repository.UserRepository;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.provider.OtpService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Spy
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OtpService otpService;
    @Mock
    private AppProperties appProperties;
    @Mock
    private StorageService storageService;
    @Mock
    private SettingService settingService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UUID userId;
    private UserEntity userEntity;
    private UserCreationRequestDto userCreationRequestDto;
    private UserEntity existingUser;
    private SignUpRequestDto signUpRequestDto;
    private SignUpMobileRequestDto signUpMobileRequestDto ;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = UserEntity.builder()
                .userId(userId)
                .userEmail("hilton.p@terv.pro")
                .passwordHash("Hil@1234")
                .userType(AppUtils.UserType.PORTAL_USER)
                .build();
        existingUser = UserEntity.builder()
                .userId(userId)
                .userEmail("hilton.p@terv.pro")
                .userIconStorageId(UUID.randomUUID())
                .build();
        signUpRequestDto = SignUpRequestDto.builder()
                .firstName("Hilton")
                .lastName("Paul")
                .build();
        signUpMobileRequestDto = SignUpMobileRequestDto.builder()
                .userId(userId)
                .userMobile("+919999999999")
                .build();
        userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setUserEmail("hilton.p@terv.pro");
        userCreationRequestDto.setPassword(Base64.getEncoder().encodeToString("Hil@1234".getBytes()));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserPrincipalEntity_WhenUserExists() {
        doReturn(userEntity).when(userService).getUserById(userId);
        UserDetails result = userService.loadUserByUsername(userId.toString());
        assertNotNull(result);
        assertInstanceOf(UserPrincipalEntity.class, result);
        assertEquals("hilton.p@terv.pro", result.getUsername());
        assertEquals("Hil@1234", result.getPassword());
    }
    @Test
    void addUser_ShouldReturnSavedUserEntity() {
        userEntity.setUserStatus(AppUtils.UserStatus.INACTIVE);
        userEntity.setPasswordHash("Hil@1234");
        when(bCryptPasswordEncoder.encode("Hil@1234")).thenReturn("SGlsQDEyMzQ=");
        when(userRepository.save(ArgumentMatchers.<UserEntity>any())).thenReturn(userEntity);
        UserEntity result = userService.addUser(userCreationRequestDto);
        assertNotNull(result.getUserId());
        assertEquals("hilton.p@terv.pro", result.getUserEmail());
        assertEquals(AppUtils.UserType.PORTAL_USER, result.getUserType());
        assertEquals(AppUtils.UserStatus.INACTIVE, result.getUserStatus());
        assertEquals("Hil@1234", result.getPasswordHash());
        verify(userRepository, times(1)).save(ArgumentMatchers.<UserEntity>any());
        }
    @Test
    void updateUser_ShouldDeleteOldIconAndReturnUpdatedEntity_WhenIconChanged() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUserIconStorageId(UUID.randomUUID());
        requestDto.setOrgId(UUID.randomUUID());
        UserEntity updatedUser = new UserEntity();
        updatedUser.setUserId(userId);
        updatedUser.setUserIconStorageId(requestDto.getUserIconStorageId());
        updatedUser.setOrgId(requestDto.getOrgId());
        doReturn(existingUser).when(userService).getUserById(userId);
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);
        UserEntity result = userService.updateUser(userId, requestDto);
        assertNotNull(result);
        assertEquals(requestDto.getUserIconStorageId(), result.getUserIconStorageId());
        assertEquals(requestDto.getOrgId(), result.getOrgId());
        verify(storageService, times(1)).deleteStorageById(any(UUID.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    @Test
    void getUserById_ShouldReturnUserEntity_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        UserEntity result = userService.getUserById(userId);
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("hilton.p@terv.pro", result.getUserEmail());
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(userId);
        });
        String expectedMessage = AppUtils.Messages.USER_NOT_FOUND_1_USER_ID.getMessage(userId);
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    void getUsers_ShouldReturnPage_ForPortalUser() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "userEmail"));
        Page<UserEntity> mockPage = new PageImpl<>(List.of(new UserEntity(), new UserEntity()));
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            utilities.when(AppUtils::getPrincipalOrgId).thenReturn(UUID.randomUUID());
            when(userRepository.search(anyString(), any(UUID.class), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<UserEntity> result = userService.getUsers(0, 10, "userEmail", Sort.Direction.ASC, "keyword");
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            verify(userRepository, times(1))
                    .search(eq("keyword"), any(UUID.class), any(Pageable.class));
        }
    }
    @Test
    void getUsers_ShouldReturnPage_ForKeycloakUser() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "userEmail"));
        Page<UserEntity> mockPage = new PageImpl<>(List.of(new UserEntity()));
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(userRepository.search(anyString(), isNull(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<UserEntity> result = userService.getUsers(0, 10, "userEmail", Sort.Direction.ASC, "keyword");
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userRepository, times(1))
                    .search(eq("keyword"), isNull(), any(Pageable.class));
        }
    }
    @Test
    void getUsers_ShouldThrowException_ForMobileUser() {
        try (MockedStatic<AppUtils> utilities = Mockito.mockStatic(AppUtils.class)) {
            utilities.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            assertThrows(AppException.class, () -> userService.getUsers(0, 10, "userEmail", Sort.Direction.ASC, "keyword"));
        }
    }
    @Test
    void createUser_ShouldReturnSignUpResponseDtoWithUserId() {
        when(appProperties.getDefaultOrganizationId()).thenReturn(UUID.randomUUID().toString());
        when(appProperties.getDefaultUserId()).thenReturn(UUID.randomUUID().toString());
        UserEntity savedEntity = UserEntity.builder()
                .userId(userId)
                .build();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        SignUpResponseDto response = userService.createUser(signUpRequestDto);
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
    }
    @Test
    void addMobile_ShouldUpdateUserAndSendOtp_WhenMobileDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUserMobileAndUserStatus(signUpMobileRequestDto.getUserMobile(), AppUtils.UserStatus.ACTIVE))
                .thenReturn(false);
        userService.addMobile(signUpMobileRequestDto);
        assertEquals(signUpMobileRequestDto.getUserMobile(), existingUser.getUserMobile());
        assertEquals(AppUtils.UserType.MOBILE_USER, existingUser.getUserType());
        assertTrue(existingUser.isSkipAudit());
        verify(otpService, times(1)).sendMobileOtp(signUpMobileRequestDto);
        verify(userRepository, times(1)).save(existingUser);
    }
    @Test
    void addMobile_ShouldThrowException_WhenMobileAlreadyExists() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.existsByUserMobileAndUserStatus(signUpMobileRequestDto.getUserMobile(), AppUtils.UserStatus.ACTIVE))
                .thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> userService.addMobile(signUpMobileRequestDto));
        assertTrue(exception.getMessage().contains("Mobile number already exists"));
        verify(otpService, never()).sendMobileOtp(any());
        verify(userRepository, never()).save(any());
    }
    @Test
    void updateMobileVerification_ShouldMarkMobileAsVerified_AndSaveUser() {
        existingUser.setMobileVerified(false);
        doReturn(existingUser).when(userService).getUserById(userId);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        userService.updateMobileVerification(userId);
        assertTrue(existingUser.isMobileVerified());
        assertTrue(existingUser.isSkipAudit());
        verify(userRepository, times(1)).save(existingUser);
    }
}
