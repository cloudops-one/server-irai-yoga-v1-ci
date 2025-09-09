package yoga.irai.server.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.RefreshTokenEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.service.JwtService;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.provider.OtpService;

/**
 * AuthenticationController handles authentication-related requests such as
 * sign-in, token refresh, and sign-out for the Irai Yoga application. It uses
 * JWT for access token management and supports device tracking.
 */
@Slf4j
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthenticationController {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserService userService;
    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates a user and generates an access token and refresh token.
     *
     * @param signInMobileRequestDto
     *            containing username, password, device code, device name, and
     *            device type.
     * @return ResponseEntity with the authentication response containing access and
     *         refresh tokens.
     */
    @PostMapping("/signin/mobile")
    @Operation(summary = "Sign In", description = "Authenticates user and generates access and refresh tokens.")
    public ResponseEntity<AppResponseDto<SignInResponseDto>> signInMobile(
            @Valid @RequestBody SignInMobileRequestDto signInMobileRequestDto) {
        UserEntity userEntity = userService.checkCredentialsByUserMobile(signInMobileRequestDto.getUserMobile());
        if (AppUtils.UserStatus.VERIFIED.equals(userEntity.getUserStatus())) {
            throw new AppException(AppUtils.Messages.PLEASE_RESET_PASSWORD.getMessage());
        }
        String username = userService.checkUserStatus(userEntity).toString();
        return signIn(username, signInMobileRequestDto.getPassword(), signInMobileRequestDto.getDeviceCode(),
                signInMobileRequestDto.getDeviceType(), signInMobileRequestDto.getDeviceName());
    }

    /**
     * Authenticates a user and generates an access token and refresh token.
     *
     * @param signInEmailRequestDto
     *            containing username, password, device code, device name, and
     *            device type.
     * @return ResponseEntity with the authentication response containing access and
     *         refresh tokens.
     */
    @PostMapping("/signin/email")
    @Operation(summary = "Sign In", description = "Authenticates user and generates access and refresh tokens.")
    public ResponseEntity<AppResponseDto<SignInResponseDto>> signInEmail(
            @Valid @RequestBody SignInEmailRequestDto signInEmailRequestDto) {
        UserEntity userEntity = userService.checkCredentials(signInEmailRequestDto.getUserEmail());
        if (AppUtils.UserStatus.VERIFIED.equals(userEntity.getUserStatus())) {
            throw new AppException(AppUtils.Messages.PLEASE_RESET_PASSWORD.getMessage());
        }
        String username = userService.checkUserStatus(userEntity).toString();
        return signIn(username, signInEmailRequestDto.getPassword(), signInEmailRequestDto.getDeviceCode(),
                signInEmailRequestDto.getDeviceType(), signInEmailRequestDto.getDeviceName());
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshTokenRequestDto
     *            containing the refresh token to validate and use for generating a
     *            new access token.
     * @return ResponseEntity with the new access token and the same refresh token
     *         if valid.
     */
    @PostMapping("/refresh/token")
    @Operation(summary = "Refresh Access Token", description = "Generates a new access token using a valid refresh token.")
    public ResponseEntity<AppResponseDto<SignInResponseDto>> refreshAccessToken(
            @Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<SignInResponseDto> builder = AppResponseDto.builder();
        if (!jwtService.isRefreshTokenValid(refreshTokenRequestDto)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(builder.message(AppUtils.Messages.REFRESH_TOKEN_IS_INVALID.getMessage()).build());
        }
        String username = jwtService.extractUsername(refreshTokenRequestDto.getRefreshToken());
        UserDetails user = userService.loadUserByUsername(username);
        String newAccessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(builder
                .data(SignInResponseDto.builder().accessToken(newAccessToken)
                        .refreshToken(refreshTokenRequestDto.getRefreshToken()).build())
                .message(AppUtils.Messages.ACCESS_TOKEN_GENERATED.getMessage()).build());
    }

    /**
     * Signs out a user by revoking their refresh token.
     *
     * @param signOutRequestDto
     *            containing the refresh token to be revoked.
     * @return ResponseEntity with a message indicating successful sign-out.
     */
    @PostMapping("/signout")
    @Operation(summary = "Sign Out", description = "Revokes the refresh token to sign out the user.")
    public ResponseEntity<AppResponseDto<Void>> signOut(@Valid @RequestBody SignOutRequestDto signOutRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        jwtService.revokeRefreshToken(signOutRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.SIGN_OUT_SUCCESS.getMessage()).build());
    }

    /**
     * Checks if the user exists for password reset and sends an OTP to the user's
     * email
     *
     * @param forgotPasswordEmailRequestDto
     *            containing email to check.
     * @return ResponseEntity with a message indicating whether the user was found
     *         or not, and sends OTP if found.
     */
    @PostMapping("/forgot/password/email")
    @Operation(summary = "Check User for Forgot Password", description = "Step 1a: Checks if the user exists for password reset.")
    public ResponseEntity<AppResponseDto<ForgotPasswordResponseDto>> forgotPasswordEmail(
            @Valid @RequestBody ForgotPasswordEmailRequestDto forgotPasswordEmailRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<ForgotPasswordResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService.checkCredentials(forgotPasswordEmailRequestDto.getUserEmail());
        UUID userId = (AppUtils.UserStatus.VERIFIED.equals(userEntity.getUserStatus()))
                ? userEntity.getUserId()
                : userService.checkUserStatus(userEntity);

        if (!userId.equals(UUID.fromString(appProperties.getE2eUserId()))) {
            otpService.sendEmailOtp(forgotPasswordEmailRequestDto.getUserEmail(), userId);
        }
        return ResponseEntity.ok(builder.message(AppUtils.Messages.USER_FOUND.getMessage())
                .data(ForgotPasswordResponseDto.builder().userId(userId).build()).build());
    }

    /**
     * Checks if the user exists for password reset and sends an OTP to the user's
     * mobile.
     *
     * @param forgotPasswordMobileRequestDto
     *            containing mobile number to check.
     * @return ResponseEntity with a message indicating whether the user was found
     *         or not, and sends OTP if found.
     */
    @PostMapping("/forgot/password/mobile")
    @Operation(summary = "Check User for Forgot Password", description = "Step 1b: Checks if the user exists for password reset.")
    public ResponseEntity<AppResponseDto<ForgotPasswordResponseDto>> forgotPasswordMobile(
            @Valid @RequestBody ForgotPasswordMobileRequestDto forgotPasswordMobileRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<ForgotPasswordResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService
                .checkCredentialsByUserMobile(forgotPasswordMobileRequestDto.getUserMobile());
        UUID userId = (AppUtils.UserStatus.VERIFIED.equals(userEntity.getUserStatus()))
                ? userEntity.getUserId()
                : userService.checkUserStatus(userEntity);
        if (!userId.equals(UUID.fromString(appProperties.getE2eUserId()))) {
            otpService.sendMobileOtp(forgotPasswordMobileRequestDto.getUserMobile(), userId);
        }
        return ResponseEntity.ok(builder.message(AppUtils.Messages.OTP_SEND_SUCCESS.getMessage())
                .data(ForgotPasswordResponseDto.builder().userId(userId).build()).build());
    }

    /**
     * Verifies the OTP sent to the user for password reset.
     *
     * @param verifyOtpRequestDto
     *            containing the OTP and user ID for verification.
     * @return ResponseEntity with a message indicating successful verification.
     */
    @PostMapping("/verify/otp")
    @Operation(summary = "Verify OTP", description = "Step 2: Verifies the OTP sent to the user for verification.")
    public ResponseEntity<AppResponseDto<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto verifyOtpRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        if (verifyOtpRequestDto.getUserId().equals(UUID.fromString(appProperties.getE2eUserId()))) {
            if (!verifyOtpRequestDto.getOtp().equalsIgnoreCase(appProperties.getE2eOtp())) {
                throw new AppException(AppUtils.Messages.INVALID_OTP.getMessage());
            }
        } else {
            otpService.verifyOtp(verifyOtpRequestDto);
        }
        userService.updateUserStatus(verifyOtpRequestDto.getUserId(), AppUtils.UserStatus.VERIFIED);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.VERIFICATION_SUCCESS.getMessage()).build());
    }

    /**
     * Updates the user's password after verifying the OTP.
     *
     * @param signUpPasswordRequestDto
     *            containing the new password and user ID for updating.
     * @return ResponseEntity with a message indicating successful password update.
     */
    @PutMapping("update/password")
    @Operation(summary = "Update Password", description = "Step 3: Updates the user's password after verification.")
    public ResponseEntity<AppResponseDto<Void>> updatePassword(
            @Valid @RequestBody SignUpPasswordRequestDto signUpPasswordRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.updatePassword(signUpPasswordRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.PASSWORD_SET_SUCCESS.getMessage()).build());
    }

    /**
     * Generates an AuthResponseDto containing access and refresh tokens after
     * successful authentication.
     *
     * @param username
     *            the username of the user.
     * @param password
     *            the password of the user.
     * @param deviceCode
     *            the unique code of the device.
     * @param deviceName
     *            the name of the device.
     * @param deviceType
     *            the type of the device (e.g., mobile, desktop).
     * @return AuthResponseDto containing access and refresh tokens.
     */
    private SignInResponseDto authenticateUser(String username, String password, String deviceCode, String deviceType,
            String deviceName) {
        password = AppUtils.decodeBase64ToString(password);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(authToken);
        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken1 = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken1);
        UserEntity user = ((UserPrincipalEntity) userDetails).user();
        DeviceEntity deviceEntity = userService.findOrCreateDevice(user, deviceCode, deviceType, deviceName);
        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshTokenEntity refreshTokenEntity = jwtService.createAndSaveRefreshToken(user, userDetails, deviceEntity);
        userService.updateLastLogin(user.getUserId());
        return SignInResponseDto.builder().accessToken(accessToken).refreshToken(refreshTokenEntity.getToken()).build();
    }

    private ResponseEntity<AppResponseDto<SignInResponseDto>> signIn(String username, String password,
            String deviceCode, String deviceType, String deviceName) {
        try {
            return ResponseEntity.ok(AppResponseDto.<SignInResponseDto>builder()
                    .data(authenticateUser(username, password, deviceCode, deviceType, deviceName))
                    .message(AppUtils.Messages.SIGN_IN_SUCCESS.getMessage()).build());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AppResponseDto.<SignInResponseDto>builder()
                    .errorMessage(AppUtils.Messages.INVALID_PASSWORD.getMessage()).build());
        }
    }
}
