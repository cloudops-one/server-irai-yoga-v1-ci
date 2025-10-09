package yoga.irai.server.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.controller.AuthenticationController;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.RefreshTokenEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.service.JwtService;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.provider.OtpService;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private OtpService otpService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private JwtService jwtService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private UserService userService;

    @Mock
    private UserEntity userEntity;

    @Mock
    private RefreshTokenEntity refreshTokenEntity;

    @Mock
    private UserDetails userDetails;

    private UUID userId;
    private SignInMobileRequestDto signInMobileRequestDto;
    private SignOutRequestDto signOutRequestDto;
    private RefreshTokenRequestDto refreshTokenRequestDto;
    private SignUpPasswordRequestDto signUpPasswordRequestDto;
    private SignInEmailRequestDto signInEmailRequestDto;
    private VerifyOtpRequestDto verifyOtpRequestDto;
    private ForgotPasswordEmailRequestDto forgotPasswordEmailRequestDto;
    private ForgotPasswordMobileRequestDto forgotPasswordMobileRequestDto;
    @BeforeEach
    void setup() throws ParseException {
        userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        UUID userIconStorageId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        forgotPasswordMobileRequestDto = ForgotPasswordMobileRequestDto.builder().userMobile("+919940798142").build();
        forgotPasswordEmailRequestDto = ForgotPasswordEmailRequestDto.builder().userEmail("hilton.p@terv.pro").build();
        verifyOtpRequestDto = VerifyOtpRequestDto.builder().userId(userId).otp("141003").build();
        signInEmailRequestDto = SignInEmailRequestDto.builder().userEmail("hilton.p@terv.pro").password("SGlsQDEyMzQ=")
                .deviceCode("WEB-18373e0c2e2c3c00155228e847ecc935").deviceName("Chrome on Win32").deviceType("WEB")
                .build();
        refreshTokenRequestDto = RefreshTokenRequestDto.builder().refreshToken(UUID.randomUUID().toString()).build();
        signUpPasswordRequestDto = SignUpPasswordRequestDto.builder()
                .userId(UUID.fromString("7f4e15a3-5b30-4060-9b96-eb9db5413ae2")).password("Hil@123").build();
        signInMobileRequestDto = SignInMobileRequestDto.builder().userMobile("+919940798142").password("SGlsQDEyMzQ=")
                .deviceCode("WEB-18373e0c2e2c3c00155228e847ecc935").deviceName("Chrome on Win32").deviceType("WEB")
                .build();
        signOutRequestDto = new SignOutRequestDto("7f4e15a3-5b30-4060-9b96-eb9db5413ae2");
        userEntity = UserEntity.builder().userId(userId).orgId(orgId).userIconStorageId(userIconStorageId)
                .userStatus(AppUtils.UserStatus.ACTIVE).userType(AppUtils.UserType.MOBILE_USER).isMobileVerified(true)
                .userFirstName("Hilton").userLastName("Paul").userEmail("hilton.p@terv.pro").userMobile("+919940798142")
                .gender(AppUtils.Gender.MALE)
                .addresses(List.of(AddressDto.builder().id(0).addressLine1("2/1").addressLine2("Church street")
                        .city("Tirunelveli").stateProvince("Tamil Nadu").postalCode("627502").country("India").build()))
                .dateOfBirth(new SimpleDateFormat("dd-MM-yyyy").parse("09-06-2003"))
                .bloodGroup(AppUtils.BloodGroup.A_POSITIVE).userAoi("Test").passwordHash("SGlsQDEyMzQ")
                .createdBy(createdBy).createdAt(ZonedDateTime.of(2025, 6, 9, 13, 33, 0, 0, ZoneId.of("Asia/Kolkata")))
                .updatedBy(updatedBy).updatedAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.of(2025, 7, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata")))
                .lastLoginAt(ZonedDateTime.of(2025, 8, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata"))).build();
    }
    @Test
    void forgotPasswordMobileTest() {
        when(userService.checkCredentialsByUserMobile(forgotPasswordMobileRequestDto.getUserMobile())).thenReturn(userEntity);
        when(userService.checkUserStatus(userEntity)).thenReturn(userId);
        when(appProperties.getE2eUserId()).thenReturn("a8672a71-0516-43b3-b115-4f5e10b12900");
        doNothing().when(otpService).sendMobileOtp(forgotPasswordMobileRequestDto.getUserMobile(), userId);
        ResponseEntity<AppResponseDto<ForgotPasswordResponseDto>> response = authenticationController.forgotPasswordMobile(forgotPasswordMobileRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
    void forgotPasswordEmailTest() {
        when(userService.checkCredentials(forgotPasswordEmailRequestDto.getUserEmail())).thenReturn(userEntity);
        when(userService.checkUserStatus(userEntity)).thenReturn(userId);
        when(appProperties.getE2eUserId()).thenReturn("a8672a71-0516-43b3-b115-4f5e10b12900");
        ResponseEntity<AppResponseDto<ForgotPasswordResponseDto>> response = authenticationController.forgotPasswordEmail(forgotPasswordEmailRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void verifyOtpTest() {
        doNothing().when(otpService).verifyOtp(verifyOtpRequestDto);
        doNothing().when(userService).updateUserStatus(userId, AppUtils.UserStatus.VERIFIED);
        when(appProperties.getE2eUserId()).thenReturn("a8672a71-0516-43b3-b115-4f5e10b12900");
        ResponseEntity<AppResponseDto<Void>> response = authenticationController.verifyOtp(verifyOtpRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }
    @Test
     void refreshAccessTokenTest() {
        when(jwtService.isRefreshTokenValid(refreshTokenRequestDto)).thenReturn(true);
        when(jwtService.extractUsername(refreshTokenRequestDto.getRefreshToken())).thenReturn(userId.toString());
        when(userService.loadUserByUsername(userId.toString())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("New access token");

         ResponseEntity<AppResponseDto<SignInResponseDto>> response = authenticationController.
                 refreshAccessToken(refreshTokenRequestDto);
         verify(jwtService).extractUsername(refreshTokenRequestDto.getRefreshToken());
         assert response.getStatusCode() == HttpStatus.OK;
     }

    @Test
    void signInEmailTesting() {
    when(userService.checkCredentials(userEntity.getUserEmail())).thenReturn(userEntity);
    when(userService.checkUserStatus(userEntity)).thenReturn(userId);
    when(userService.loadUserByUsername(userId.toString())).thenReturn(new UserPrincipalEntity(userEntity));
    when(userService.findOrCreateDevice(userEntity ,
            signInEmailRequestDto.getDeviceCode() ,
            signInEmailRequestDto.getDeviceType(),
            signInEmailRequestDto.getDeviceName()
    )).thenReturn(DeviceEntity.builder().build());
    when(jwtService.generateAccessToken(userDetails)).thenReturn("Mocked AccessToken");
        when(jwtService.createAndSaveRefreshToken(any(UserEntity.class) ,
                any(UserDetails.class) ,
                any(DeviceEntity.class))).thenReturn(refreshTokenEntity);

    String token = jwtService.generateAccessToken(userDetails);
    assertEquals("Mocked AccessToken", token);
    ResponseEntity<AppResponseDto<SignInResponseDto>> response = authenticationController.signInEmail(signInEmailRequestDto);
    assert response.getStatusCode() ==  HttpStatus.OK;
    }

    @Test
    void testingSignInMobile() {
        when(userService.checkCredentialsByUserMobile(userEntity.getUserMobile())).thenReturn(userEntity);
        when(userService.checkUserStatus(userEntity)).thenReturn(userId);
        when(userService.loadUserByUsername(userId.toString())).
                thenReturn(new UserPrincipalEntity(userEntity));
        when(userService.findOrCreateDevice(
                userEntity,
                signInMobileRequestDto.getDeviceCode(),
                signInMobileRequestDto.getDeviceType(),
                signInMobileRequestDto.getDeviceName()
        )).thenReturn(DeviceEntity.builder().build());
        when(jwtService.generateAccessToken(userDetails))
                .thenReturn("mocked jwt token");
        when(jwtService.createAndSaveRefreshToken(any(UserEntity.class) ,
                any(UserDetails.class),
                any(DeviceEntity.class)))
                .thenReturn(refreshTokenEntity);

        String token = jwtService.generateAccessToken(userDetails);
        assertEquals("mocked jwt token", token);
        ResponseEntity<AppResponseDto<SignInResponseDto>> response = authenticationController.signInMobile(signInMobileRequestDto);
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdatePasswordSuccess() {
        doNothing().when(userService).updatePassword(signUpPasswordRequestDto);
        ResponseEntity<AppResponseDto<Void>> response = authenticationController
                .updatePassword(signUpPasswordRequestDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.PASSWORD_SET_SUCCESS.getMessage(), response.getBody().getMessage());
    }

    @Test
    void signOutTest() {
        doNothing().when(jwtService).revokeRefreshToken(signOutRequestDto);
        ResponseEntity<AppResponseDto<Void>> response = authenticationController.signOut(signOutRequestDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppUtils.Messages.SIGN_OUT_SUCCESS.getMessage(), response.getBody().getMessage());
    }
}
