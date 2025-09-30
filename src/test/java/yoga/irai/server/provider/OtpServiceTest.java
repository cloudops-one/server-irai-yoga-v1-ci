package yoga.irai.server.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.dto.SignUpEmailRequestDto;
import yoga.irai.server.authentication.dto.SignUpMobileRequestDto;
import yoga.irai.server.authentication.dto.VerifyOtpRequestDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private SmsService smsService;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpRepository otpRepository;

    @InjectMocks
    private OtpService otpService;

    private UUID userId;
    private OtpEntity otpEntity;
    private VerifyOtpRequestDto verifyOtpRequestDto;
    private SignUpMobileRequestDto signUpMobileRequestDto;
    private SignUpEmailRequestDto signUpEmailRequestDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otpEntity = OtpEntity.builder()
                .userId(userId)
                .otpNumber("999999")
                .otpId(UUID.randomUUID())
                .expiryTime(ZonedDateTime.now().plusMinutes(5))
                .build();
        verifyOtpRequestDto = VerifyOtpRequestDto.builder()
                .otp("999999").userId(userId)
                .build();
        signUpMobileRequestDto = SignUpMobileRequestDto.builder()
                .userMobile("+919999999999")
                .userId(userId)
                .build();
        signUpEmailRequestDto = SignUpEmailRequestDto.builder()
                .userId(userId)
                .email("test@irai.yoga")
                .build();
        ReflectionTestUtils.setField(otpService, "appOtpExpiryMinutes", 5);
    }

    @Test
    void testVerifyOtp() {
        when(otpRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(otpEntity));
        doNothing().when(otpRepository).deleteById(otpEntity.getOtpId());
        otpService.verifyOtp(verifyOtpRequestDto);
        verify(otpRepository).deleteById(any(UUID.class));
    }

    @Test
    void testVerifyOtp_Expired() {
        otpEntity.setExpiryTime(ZonedDateTime.now().minusMinutes(5));
        when(otpRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(otpEntity));
        doNothing().when(otpRepository).deleteById(otpEntity.getOtpId());
        AppException exception = assertThrows(AppException.class, () -> otpService.verifyOtp(verifyOtpRequestDto));
        assertEquals(AppUtils.Messages.OTP_EXPIRED.getMessage(), exception.getMessage());
        verify(otpRepository).deleteById(any(UUID.class));
    }

    @Test
    void testVerifyOtp_Invalid() {
        verifyOtpRequestDto.setOtp("000000");
        when(otpRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(otpEntity));
        AppException exception = assertThrows(AppException.class, () -> otpService.verifyOtp(verifyOtpRequestDto));
        assertEquals(AppUtils.Messages.INVALID_OTP.getMessage(), exception.getMessage());
    }

    @Test
    void testSendMobileOtp() {
        doNothing().when(smsService).sendOtpSms(anyString(), anyString(), anyInt());
        when(otpRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of(otpEntity));
        doNothing().when(otpRepository).deleteById(any(UUID.class));
        otpService.sendMobileOtp(signUpMobileRequestDto);
        verify(smsService).sendOtpSms(anyString(), anyString(), anyInt());
    }

    @Test
    void testSendEmailOtp() {
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyInt());
        when(otpRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of(otpEntity));
        doNothing().when(otpRepository).deleteById(any(UUID.class));
        otpService.sendEmailOtp(signUpEmailRequestDto);
        verify(emailService).sendOtpEmail(anyString(), anyString(), anyInt());
    }

    @Test
    void testGenerateOtpEntity() {
        when(otpRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());
        String otp = otpService.getOtpEntity(userId);
        verify(otpRepository).findAllByUserId(any(UUID.class));
        assert otp != null;
    }

    @Test
    void testSendMobileOtp_mobile_userId() {
        doNothing().when(smsService).sendOtpSms(anyString(), anyString(), anyInt());
        when(otpRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of(otpEntity));
        doNothing().when(otpRepository).deleteById(any(UUID.class));
        otpService.sendMobileOtp("+919999999999",userId);
        verify(smsService).sendOtpSms(anyString(), anyString(), anyInt());
    }

    @Test
    void testSendEmailOtp_email_userId() {
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyInt());
        when(otpRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of(otpEntity));
        doNothing().when(otpRepository).deleteById(any(UUID.class));
        otpService.sendEmailOtp("test@irai.yoga",userId);
        verify(emailService).sendOtpEmail(anyString(), anyString(), anyInt());
    }
}
