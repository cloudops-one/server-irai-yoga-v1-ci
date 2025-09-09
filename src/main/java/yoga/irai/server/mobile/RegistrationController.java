package yoga.irai.server.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.provider.OtpService;

/**
 * RegistrationController handles user registration, including sign-up, mobile
 * and email verification, and password setup. It provides endpoints for
 * creating a user, adding mobile/email, verifying OTPs, and resending OTPs.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/mobile/register")
@AllArgsConstructor
@Tag(name = "Registration Controller", description = "Mobile user registration and verification endpoints")
public class RegistrationController {

    private final OtpService otpService;
    private final UserService userService;

    /**
     * Creates a new user with the provided sign-up details.
     *
     * @param signUpRequestDto
     *            the sign-up details
     * @return ResponseEntity containing the API response with user creation status
     */
    @PostMapping("/signup")
    @Operation(summary = "1. Create a new user", description = "Endpoint to create a new user with sign-up details. "
            + "Returns user creation status and user details if successful.")
    public ResponseEntity<AppResponseDto<SignUpResponseDto>> createUser(
            @Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<SignUpResponseDto> builder = AppResponseDto.builder();
        SignUpResponseDto signUpResponseDto;
        signUpResponseDto = userService.createUser(signUpRequestDto);
        return ResponseEntity
                .ok(builder.data(signUpResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Adds a mobile number to the user profile.
     *
     * @param signUpMobileRequestDto
     *            the mobile sign-up details
     * @return ResponseEntity containing the API response with mobile addition
     *         status
     */
    @PostMapping("/signup/mobile")
    @Operation(summary = "2. Add mobile number to user profile", description = "Endpoint to add a mobile number to the user profile. "
            + "Returns mobile addition status.")
    public ResponseEntity<AppResponseDto<Void>> addMobile(
            @Valid @RequestBody SignUpMobileRequestDto signUpMobileRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.addMobile(signUpMobileRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Verifies the mobile OTP for the user.
     *
     * @param verifyOtpRequestDto
     *            the mobile verification details
     * @return ResponseEntity containing the API response with mobile verification
     *         status
     */
    @PostMapping("/verify/mobile")
    @Operation(summary = "3. Verify mobile OTP", description = "Endpoint to verify the mobile OTP for the user. "
            + "Returns mobile verification status.")
    public ResponseEntity<AppResponseDto<Void>> verifyMobileOtp(
            @Valid @RequestBody VerifyOtpRequestDto verifyOtpRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        otpService.verifyOtp(verifyOtpRequestDto);
        userService.updateMobileVerification(verifyOtpRequestDto.getUserId());
        return ResponseEntity.ok(builder.message(
                AppUtils.Messages.MOBILE_VERIFICATION_SUCCESS_1_USER_ID.getMessage(verifyOtpRequestDto.getUserId()))
                .build());
    }

    /**
     * Resends the mobile OTP to the user.
     *
     * @param signUpMobileRequestDto
     *            the mobile sign-up details
     * @return ResponseEntity containing the API response with OTP resend status
     */
    @PostMapping("/resend/mobile")
    @Operation(summary = "Resend mobile OTP", description = "Endpoint to resend the mobile OTP to the user. "
            + "Returns OTP resend status.")
    public ResponseEntity<AppResponseDto<Void>> resendMobileOtp(
            @Valid @RequestBody SignUpMobileRequestDto signUpMobileRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        otpService.sendMobileOtp(signUpMobileRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.OTP_RESEND_SUCCESS.getMessage()).build());
    }

    /**
     * Adds an email to the user profile.
     *
     * @param signUpEmailRequestDto
     *            the email sign-up details
     * @return ResponseEntity containing the API response with email addition status
     */
    @PostMapping("/signup/email")
    @Operation(summary = "4. Add email to user profile", description = "Endpoint to add an email to the user profile. "
            + "Returns email addition status and sends OTP for verification.")
    public ResponseEntity<AppResponseDto<Void>> addEmail(
            @Valid @RequestBody SignUpEmailRequestDto signUpEmailRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.addEmail(signUpEmailRequestDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Verifies the email OTP for the user.
     *
     * @param verifyOtpRequestDto
     *            the email verification details
     * @return ResponseEntity containing the API response with email verification
     *         status
     */
    @PostMapping("/verify/email")
    @Operation(summary = "5. Verify email OTP", description = "Endpoint to verify the email OTP for the user. "
            + "Returns email verification status.")
    public ResponseEntity<AppResponseDto<Void>> verifyEmailOtp(
            @Valid @RequestBody VerifyOtpRequestDto verifyOtpRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        otpService.verifyOtp(verifyOtpRequestDto);
        userService.updateEmailVerification(verifyOtpRequestDto.getUserId());
        return ResponseEntity.ok(builder
                .message(AppUtils.Messages.VERIFICATION_SUCCESS.getMessage(verifyOtpRequestDto.getUserId())).build());
    }

    /**
     * Resends the email OTP to the user.
     *
     * @param signUpEmailRequestDto
     *            the email sign-up details
     * @return ResponseEntity containing the API response with OTP resend status
     */
    @PostMapping("/resend/email")
    @Operation(summary = "Resend email OTP", description = "Endpoint to resend the email OTP to the user. "
            + "Returns OTP resend status.")
    public ResponseEntity<AppResponseDto<Void>> resendEmailOtp(
            @Valid @RequestBody SignUpEmailRequestDto signUpEmailRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        otpService.sendEmailOtp(signUpEmailRequestDto);
        return ResponseEntity.ok(builder
                .message(AppUtils.Messages.OTP_RESEND_SUCCESS.getMessage(signUpEmailRequestDto.getUserId())).build());
    }

    /**
     * Sets the password for the user.
     *
     * @param signUpPasswordRequestDto
     *            the password sign-up details
     * @return ResponseEntity containing the API response with password setup status
     */
    @PostMapping("/signup/password")
    @Operation(summary = "6. Set user password", description = "Endpoint to set the password for the user. "
            + "Returns password setup status.")
    public ResponseEntity<AppResponseDto<Void>> addPassword(
            @Valid @RequestBody SignUpPasswordRequestDto signUpPasswordRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.savePassword(signUpPasswordRequestDto);
        return ResponseEntity.ok(
                builder.message(AppUtils.Messages.PASSWORD_SET_SUCCESS.getMessage(signUpPasswordRequestDto.getUserId()))
                        .build());
    }
}
