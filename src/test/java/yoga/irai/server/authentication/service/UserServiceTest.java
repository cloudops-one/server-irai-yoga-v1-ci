package yoga.irai.server.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;
import yoga.irai.server.authentication.repository.UserRepository;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.provider.OtpService;
import yoga.irai.server.setting.SettingEntity;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

import java.time.Instant;
import java.util.*;

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
    private DeviceEntity deviceEntity;
    private SignUpRequestDto signUpRequestDto;
    private SignUpMobileRequestDto signUpMobileRequestDto;
    private SignUpEmailRequestDto signUpEmailRequestDto;
    private SignUpPasswordRequestDto signUpPasswordRequestDto;
    private UpdatePasswordDto updatePasswordDto;
    private UserStatsDto userStatsDto;
    private SettingEntity settingEntity;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = UserEntity.builder()
                .userId(userId)
                .userFirstName("Hilton")
                .userLastName("Paul")
                .userEmail("hilton.p@terv.pro")
                .passwordHash("Hil@1234")
                .userType(AppUtils.UserType.PORTAL_USER)
                .userAoi("1:2;2:1;3:1")
                .build();
        userEntity.setCreatedBy(userId);
        userEntity.setUpdatedBy(userId);
        existingUser = UserEntity.builder()
                .userId(userId)
                .userFirstName("Hilton")
                .userLastName("Paul")
                .userEmail("hilton.p@terv.pro")
                .userIconStorageId(UUID.randomUUID())
                .build();
        deviceEntity = DeviceEntity.builder()
                .fcmToken("1234").user(userEntity)
                .deviceCode(UUID.randomUUID().toString())
                .deviceType("device")
                .build();
        signUpRequestDto = SignUpRequestDto.builder()
                .firstName("Hilton")
                .lastName("Paul")
                .build();
        signUpMobileRequestDto = SignUpMobileRequestDto.builder()
                .userId(userId)
                .userMobile("+919999999999")
                .build();
        signUpEmailRequestDto = SignUpEmailRequestDto.builder()
                .userId(userId)
                .email("hilton.p@terv.pro")
                .build();
        signUpPasswordRequestDto = SignUpPasswordRequestDto.builder()
                .userId(userId)
                .password("SGlsQDEyMzQ")
                .build();
        updatePasswordDto = UpdatePasswordDto.builder()
                .newPassword("SGlsQDEyMzQ")
                .oldPassword("SGlsQDEyMzQ")
                .build();
        userStatsDto = UserStatsDto.builder()
                .totalActiveMobileUsers(10L)
                .totalActiveUsers(10L)
                .totalUser(10L)
                .totalMobileUsers(10L)
                .build();
        settingEntity = SettingEntity.builder()
                .settingValue("[{\"questionId\":1,\"status\":\"ACTIVE\",\"questionName\":\"I am here for\",\"optionType\":\"MULTIPLE\",\"options\":[{\"id\":1,\"value\":\"Yoga Practices\"},{\"id\":2,\"value\":\"Guided Meditation\"},{\"id\":3,\"value\":\"VeWa Wisdom\"}]},{\"questionId\":2,\"status\":\"ACTIVE\",\"questionName\":\"I need Vewa Guidance on\",\"optionType\":\"MULTIPLE\",\"max\":3,\"options\":[{\"id\":1,\"value\":\"Accelerating my spiritual growth\"},{\"id\":2,\"value\":\"Becoming more joyful\"},{\"id\":3,\"value\":\"Dealing with my stress & anxiety\"},{\"id\":4,\"value\":\"Sleeping better\"},{\"id\":5,\"value\":\"Boosting my energy\"},{\"id\":6,\"value\":\"Improving my relationships\"},{\"id\":7,\"value\":\"Being more successful\"},{\"id\":8,\"value\":\"Increasing my productivity\"},{\"id\":9,\"value\":\"Addressing my health concerns\"},{\"id\":10,\"value\":\"Living more consciously\"}]},{\"questionId\":3,\"status\":\"ACTIVE\",\"questionName\":\"I am___ Years of age\",\"optionType\":\"SINGLE\",\"options\":[{\"id\":1,\"value\":\"Under 18\"},{\"id\":2,\"value\":\"Between 18 and 24\"},{\"id\":3,\"value\":\"Between 25 and 34\"},{\"id\":4,\"value\":\"Between 35 and 44\"},{\"id\":5,\"value\":\"Between 45 and 54\"},{\"id\":6,\"value\":\"Above 55\"},{\"id\":7,\"value\":\"Prefer not to answer\"}]},{\"questionId\":4,\"status\":\"ACTIVE\",\"questionName\":\"My connection with vewa\",\"optionType\":\"MULTIPLE\",\"options\":[{\"id\":1,\"value\":\"I’ve  heard about Vewa and want to know more\"},{\"id\":2,\"value\":\"I’ve been watching Vewa Videos\"},{\"id\":3,\"value\":\"I do yoga practices regularly\"},{\"id\":4,\"value\":\"I am an vewa volunteer\"},{\"id\":5,\"value\":\"i’d learned yoga practices but i have lost touch since\"}]}]")
                .settingName("UserAoiQuestions")
                .settingStatus(AppUtils.SettingStatus.ACTIVE)
                .settingId(UUID.randomUUID())
                .build();
        userCreationRequestDto = new UserCreationRequestDto();
        userCreationRequestDto.setUserEmail("hilton.p@terv.pro");
        userCreationRequestDto.setPassword(Base64.getEncoder().encodeToString("Hil@1234".getBytes()));
        jwt = new Jwt(
                "test-token-value",                       // token value
                Instant.now(),                            // issued at
                Instant.now().plusSeconds(3600),          // expires at
                Map.of("alg", "RS256"),                   // headers
                Map.of(                              // claims
                        "email", "test@example.com",
                        "given_name", "John",
                        "family_name", "Doe",
                        "mobile", "9876543210",
                        "sub", userId
                )                                    // claims
        );
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
        when(userRepository.save(any())).thenReturn(userEntity);
        UserEntity result = userService.addUser(userCreationRequestDto);
        assertNotNull(result.getUserId());
        assertEquals("hilton.p@terv.pro", result.getUserEmail());
        assertEquals(AppUtils.UserType.PORTAL_USER, result.getUserType());
        assertEquals(AppUtils.UserStatus.INACTIVE, result.getUserStatus());
        assertEquals("Hil@1234", result.getPasswordHash());
        verify(userRepository, times(1)).save(any());
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
        verify(storageService, times(1)).deleteStorageById(any(UUID.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void updateUser_NoChanged() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUserIconStorageId(UUID.randomUUID());
        requestDto.setOrgId(UUID.randomUUID());
        UserEntity updatedUser = new UserEntity();
        updatedUser.setUserId(userId);
        updatedUser.setUserIconStorageId(UUID.randomUUID());
        updatedUser.setOrgId(requestDto.getOrgId());
        doReturn(existingUser).when(userService).getUserById(userId);
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);
        UserEntity result = userService.updateUser(userId, requestDto);
        assertNotNull(result);
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
        Exception exception = assertThrows(RuntimeException.class, () ->
                userService.getUserById(userId));
        String expectedMessage = AppUtils.Messages.USER_NOT_FOUND_1_USER_ID.getMessage(userId);
        assertTrue(exception.getMessage().contains(expectedMessage));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUsers_ShouldReturnPage_ForPortalUser() {
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

    @Test
    void addEmail_ShouldUpdateUserAndSendOtp_WhenEmailDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.existsByUserEmailAndUserStatus(anyString(), any(AppUtils.UserStatus.class)))
                .thenReturn(false);
        doNothing().when(otpService).sendEmailOtp(any());
        userService.addEmail(signUpEmailRequestDto);
        verify(otpService, times(1)).sendEmailOtp(any(SignUpEmailRequestDto.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(userRepository, times(1)).existsByUserEmailAndUserStatus(anyString(), any(AppUtils.UserStatus.class));
    }

    @Test
    void addEmail_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(userRepository.existsByUserEmailAndUserStatus(anyString(), any(AppUtils.UserStatus.class)))
                .thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> userService.addEmail(signUpEmailRequestDto));
        assertTrue(exception.getMessage().contains(AppUtils.Messages.EMAIL_ALREADY_EXISTS.getMessage(userId)));
        verify(otpService, never()).sendMobileOtp(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmailVerification_ShouldMarkEmailAsVerified_AndSaveUser() {
        existingUser.setEmailVerified(false);
        doReturn(existingUser).when(userService).getUserById(userId);
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        userService.updateEmailVerification(userId);
        assertTrue(existingUser.isEmailVerified());
        assertTrue(existingUser.isSkipAudit());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testSavePassword() {
        when(userRepository.findById(any(UUID.class))).thenReturn(java.util.Optional.of(existingUser));
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("Hil@1234");
        userService.savePassword(signUpPasswordRequestDto);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testGetUserNameById() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(existingUser));
        String userName = userService.getUserNameById(userId);
        assertEquals(userEntity.getUserFirstName() + " " + userEntity.getUserLastName(), userName);
        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testGetUserNameById_NoUserFound() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        String userName = userService.getUserNameById(userId);
        assertEquals("", userName);
        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testGetUserNamesByIds() {
        when(userRepository.findAllById(anyList())).thenReturn(List.of(existingUser));
        Map<UUID, String> userEntities = userService.getUserNamesByIds(List.of(userId));
        verify(userRepository, times(1)).findAllById((anyList()));
        assert userEntities.size() == 1;
    }

    @Test
    void testUpdateLastLogin() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(existingUser));
        userService.updateLastLogin(userId);
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCheckCredentials() {
        when(userRepository.findByUserEmail(anyString())).thenReturn(userEntity);
        UserEntity user = userService.checkCredentials("hilton.p@terv.pro");
        assertEquals(user, userEntity);
        verify(userRepository, times(1)).findByUserEmail(anyString());
    }

    @Test
    void testCheckCredentials_UserNotFound() {
        when(userRepository.findByUserEmail(anyString())).thenReturn(null);
        AppException exception = assertThrows(AppException.class, () ->
                userService.checkCredentials("hilton.p@terv.pro")
        );
        assertTrue(exception.getMessage().contains(AppUtils.Messages.USER_NOT_FOUND.getMessage(userId)));
    }

    @Test
    void testCheckCredentialsByUserMobile() {
        when(userRepository.findByUserMobile(anyString())).thenReturn(userEntity);
        UserEntity user = userService.checkCredentialsByUserMobile("");
        assertEquals(user, userEntity);
        verify(userRepository, times(1)).findByUserMobile(anyString());
    }

    @Test
    void testCheckCredentialsByUserMobile_UserNotFound() {
        when(userRepository.findByUserMobile(anyString())).thenReturn(null);
        AppException exception = assertThrows(AppException.class, () ->
                userService.checkCredentialsByUserMobile("+919999999999"));
        assertTrue(exception.getMessage().contains(AppUtils.Messages.USER_NOT_FOUND.getMessage(userId)));
    }

    @Test
    void testUpdateUserStatus() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        userService.updateUserStatus(userId, AppUtils.UserStatus.ACTIVE);
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void getFindOrCreateDevice() {
        when(deviceRepository.findByDeviceCodeAndUser(anyString(), any(UserEntity.class))).thenReturn(Optional.ofNullable(deviceEntity));
        userService.findOrCreateDevice(userEntity, "1234", "device", "deviceName");
        verify(deviceRepository, times(1)).findByDeviceCodeAndUser(anyString(), any(UserEntity.class));
    }

    @Test
    void getFindOrCreateDevice_NewDeviceEntity() {
        when(deviceRepository.findByDeviceCodeAndUser(anyString(), any(UserEntity.class))).thenReturn(Optional.empty());
        userService.findOrCreateDevice(userEntity, "1234", "device", "deviceName");
        verify(deviceRepository, times(1)).findByDeviceCodeAndUser(anyString(), any(UserEntity.class));
    }

    @Test
    void testUpdatePassword_UserNotFound_ShouldThrowException() {
        when(userRepository.getUserEntityByUserIdAndUserStatus(any(), eq(AppUtils.UserStatus.VERIFIED)))
                .thenReturn(null);

        AppException ex = assertThrows(AppException.class,
                () -> userService.updatePassword(signUpPasswordRequestDto));

        assertEquals(AppUtils.Messages.PLEASE_RESET_PASSWORD.getMessage(), ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdatePassword_UserFound_NotE2EUser_ShouldUpdatePasswordAndSave() {
        when(userRepository.getUserEntityByUserIdAndUserStatus(any(), eq(AppUtils.UserStatus.VERIFIED)))
                .thenReturn(userEntity);
        when(appProperties.getE2eUserId()).thenReturn(UUID.randomUUID().toString());
        when(bCryptPasswordEncoder.encode(any())).thenReturn("hashedPassword");

        userService.updatePassword(signUpPasswordRequestDto);
        assertEquals(AppUtils.UserStatus.ACTIVE, userEntity.getUserStatus());
        verify(userRepository).save(userEntity);
    }

    @Test
    void testUpdatePassword_UserFound_E2EUser_ShouldNotUpdatePasswordButSave() {
        UUID e2eUserId = signUpPasswordRequestDto.getUserId();
        when(userRepository.getUserEntityByUserIdAndUserStatus(any(), eq(AppUtils.UserStatus.VERIFIED)))
                .thenReturn(userEntity);
        when(appProperties.getE2eUserId()).thenReturn(e2eUserId.toString());
        userService.updatePassword(signUpPasswordRequestDto);
        assertEquals(AppUtils.UserStatus.ACTIVE, userEntity.getUserStatus());
        verify(userRepository).save(userEntity);
    }

    @Test
    void testResetPassword() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("hashedPassword");
        userService.resetUserPassword(userId, updatePasswordDto);
        verify(bCryptPasswordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testResetPassword_InValidPassword() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> userService.resetUserPassword(userId, updatePasswordDto));
        assertEquals(AppUtils.Messages.INVALID_PASSWORD.getMessage(), exception.getMessage());
    }

    @Test
    void testGetUserStats_KeycloakUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(UUID.randomUUID());
            when(appProperties.getDefaultOrganizationId()).thenReturn(UUID.randomUUID().toString());
            when(AppUtils.getPrincipalUserType()).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(userRepository.getUserStats(any(), any(AppUtils.UserStatus.class), any(AppUtils.UserType.class))).thenReturn(userStatsDto);
            UserStatsDto userStats = userService.getUserStats();
            assert userStats != null;
            verify(userRepository, times(1)).getUserStats(any(), any(AppUtils.UserStatus.class), any(AppUtils.UserType.class));
        }
    }

    @Test
    void testGetUserStats_PortalUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(UUID.randomUUID());
            when(appProperties.getDefaultOrganizationId()).thenReturn(UUID.randomUUID().toString());
            when(AppUtils.getPrincipalUserType()).thenReturn(AppUtils.UserType.PORTAL_USER);
            when(userRepository.getUserStats(any(), any(AppUtils.UserStatus.class), any(AppUtils.UserType.class))).thenReturn(userStatsDto);
            UserStatsDto userStats = userService.getUserStats();
            assert userStats != null;
            verify(userRepository, times(1)).getUserStats(any(), any(AppUtils.UserStatus.class), any(AppUtils.UserType.class));
        }
    }

    @Test
    void testGetUserStats_MobileUser() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalOrgId).thenReturn(UUID.randomUUID());
            when(appProperties.getDefaultOrganizationId()).thenReturn(UUID.randomUUID().toString());
            when(AppUtils.getPrincipalUserType()).thenReturn(AppUtils.UserType.MOBILE_USER);
            AppException exception = assertThrows(AppException.class, () -> userService.getUserStats());
            assertEquals(AppUtils.Messages.ACCESS_DENIED.getMessage(), exception.getMessage());
        }
    }

    @Test
    void testUpdateUserAoiAnswer() {
        try (MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserId).thenReturn(UUID.randomUUID());
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
            userService.updateUserAoiAnswers("answer");
            verify(userRepository, times(1)).findById(any(UUID.class));
        }
    }

    @Test
    void testGetUserAoi() {
        when(settingService.getSettingBySettingName("UserAoiQuestions")).thenReturn(settingEntity);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        List<UserAoiDto> userAoiDtos = userService.getUserAoi(userEntity);
        verify(userRepository, times(1)).findById(any(UUID.class));
        assert userAoiDtos != null;
    }

    @Test
    void testCheckUserStatus_Active() {
        userEntity.setUserStatus(AppUtils.UserStatus.ACTIVE);
        UUID userID = userService.checkUserStatus(userEntity);
        assert userID != null;
        assertEquals(AppUtils.UserStatus.ACTIVE, userEntity.getUserStatus());
    }

    @Test
    void testCheckUserStatus_InActive() {
        userEntity.setUserStatus(AppUtils.UserStatus.INACTIVE);
        AppException exception = assertThrows(AppException.class, () -> userService.checkUserStatus(userEntity));
        assertEquals(exception.getMessage(), AppUtils.Messages.USER_ACCOUNT_DEACTIVATED.getMessage());
    }

    @Test
    void testGetUserDto() {
        userEntity.setOrgId(UUID.randomUUID());
        userEntity.setUserIconStorageId(UUID.randomUUID());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
        when(organizationService.getOrgNameByOrgId(any(UUID.class))).thenReturn("orgName");
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(any(UUID.class))).thenReturn("iconUrl");
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("storageUrl");
        UserResponseDto userResponseDto = userService.getUserDto(userEntity);
        assert userResponseDto != null;
        verify(userRepository, times(2)).findById(any(UUID.class));
    }

    @Test
    void testGetUserData() {
        when(userRepository.findAllById(anyList())).thenReturn(Collections.singletonList(userEntity));
        Map<UUID, String> userMap = userService.getUserData(List.of(settingEntity));
        verify(userRepository, times(1)).findAllById(anyList());
        assert userMap != null;
    }

    @Test
    void testCreateKeyCloakUser() {
        when(appProperties.getDefaultOrganizationId()).thenReturn(UUID.randomUUID().toString());
        when(appProperties.getDefaultUserId()).thenReturn(UUID.randomUUID().toString());
        userService.createUserFromKeycloak(jwt);
        verify(userRepository, times(1)).save(any());
    }
}
