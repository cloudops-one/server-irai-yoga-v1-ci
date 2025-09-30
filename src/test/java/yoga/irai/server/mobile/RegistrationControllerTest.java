package yoga.irai.server.mobile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.provider.OtpService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private OtpService otpService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RegistrationController registrationController;

    private SignUpRequestDto signUpRequestDto;
    private VerifyOtpRequestDto verifyOtpRequestDto;
    private SignUpEmailRequestDto signUpEmailRequestDto;
    private SignUpMobileRequestDto signUpMobileRequestDto;
    private SignUpPasswordRequestDto signUpPasswordRequestDto;

    private SignUpResponseDto signUpResponseDto;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        signUpRequestDto = SignUpRequestDto.builder()
                .firstName("firstName").lastName("lastName")
                .build();
        signUpMobileRequestDto = SignUpMobileRequestDto.builder()
                .userId(userId)
                .userMobile("+919999999999")
                .build();
        signUpEmailRequestDto = SignUpEmailRequestDto.builder()
                .userId(userId)
                .email("test@irai.yoga")
                .build();
        signUpPasswordRequestDto = SignUpPasswordRequestDto.builder()
                .userId(userId)
                .password("cGFzc3dvcmQ")
                .build();
        verifyOtpRequestDto = VerifyOtpRequestDto.builder()
                .userId(userId).otp("999999")
                .build();
        signUpResponseDto = SignUpResponseDto.builder()
                .userId(userId)
                .build();
    }

    @Test
    void testCreateUser() {
        when(userService.createUser(any(SignUpRequestDto.class))).thenReturn(signUpResponseDto);
        ResponseEntity<AppResponseDto<SignUpResponseDto>> response = registrationController.createUser(signUpRequestDto);
        verify(userService).createUser(any(SignUpRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testAddMobile() {
        doNothing().when(userService).addMobile(any(SignUpMobileRequestDto.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.addMobile(signUpMobileRequestDto);
        verify(userService).addMobile(any(SignUpMobileRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testVerifyMobileOtp() {
        doNothing().when(otpService).verifyOtp(any(VerifyOtpRequestDto.class));
        doNothing().when(userService).updateMobileVerification(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.verifyMobileOtp(verifyOtpRequestDto);
        verify(otpService).verifyOtp(any(VerifyOtpRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testAddEmail() {
        doNothing().when(userService).addEmail(any(SignUpEmailRequestDto.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.addEmail(signUpEmailRequestDto);
        verify(userService).addEmail(any(SignUpEmailRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testVerifyEmailOtp() {
        doNothing().when(otpService).verifyOtp(any(VerifyOtpRequestDto.class));
        doNothing().when(userService).updateEmailVerification(any(UUID.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.verifyEmailOtp(verifyOtpRequestDto);
        verify(otpService).verifyOtp(any(VerifyOtpRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testAddPassword() {
        doNothing().when(userService).savePassword(any(SignUpPasswordRequestDto.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.addPassword(signUpPasswordRequestDto);
        verify(userService).savePassword(any(SignUpPasswordRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testResendMobileOtp() {
        doNothing().when(otpService).sendMobileOtp(any(SignUpMobileRequestDto.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.resendMobileOtp(signUpMobileRequestDto);
        verify(otpService).sendMobileOtp(any(SignUpMobileRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testResendEmailOtp() {
        doNothing().when(otpService).sendEmailOtp(any(SignUpEmailRequestDto.class));
        ResponseEntity<AppResponseDto<Void>> response = registrationController.resendEmailOtp(signUpEmailRequestDto);
        verify(otpService).sendEmailOtp(any(SignUpEmailRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }
}
