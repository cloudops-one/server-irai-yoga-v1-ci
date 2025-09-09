package yoga.irai.server.provider;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.dto.SignUpEmailRequestDto;
import yoga.irai.server.authentication.dto.SignUpMobileRequestDto;
import yoga.irai.server.authentication.dto.VerifyOtpRequestDto;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final SmsService smsService;
    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry.minutes:5}")
    private int appOtpExpiryMinutes;

    /**
     * Generates a new OTP for the given OtpEntity and saves it to the repository.
     *
     * @param otpEntity
     *            the audit for which the OTP is generated
     * @return the generated OTP as a string
     */
    public String generateOtp(OtpEntity otpEntity) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        ZonedDateTime expiration = ZonedDateTime.now().plusMinutes(appOtpExpiryMinutes);
        otpEntity.setOtpNumber(otp);
        otpEntity.setExpiryTime(expiration);
        otpRepository.save(otpEntity);
        return otp;
    }

    /**
     * Verifies the OTP for the given user ID and OTP string.
     *
     * @param verifyOtpRequestDto
     *            the request containing user ID and OTP
     */
    @Transactional
    public void verifyOtp(VerifyOtpRequestDto verifyOtpRequestDto) {
        validateOtp(verifyOtpRequestDto.getUserId(), verifyOtpRequestDto.getOtp());
    }

    /**
     * Validates the OTP for the given user ID and OTP string.
     *
     * @param key
     *            the user ID
     * @param otp
     *            the OTP to validate
     */
    public void validateOtp(UUID key, String otp) {
        OtpEntity otpEntity = otpRepository.findByUserId(key).orElseThrow(AppUtils.Messages.INVALID_OTP::getException);
        if (otpEntity.getExpiryTime().isBefore(ZonedDateTime.now())) {
            otpRepository.deleteById(otpEntity.getOtpId());
            throw AppUtils.Messages.OTP_EXPIRED.getException();
        }
        if (!otpEntity.getOtpNumber().equals(otp)) {
            throw AppUtils.Messages.INVALID_OTP.getException();
        }
        otpRepository.deleteById(otpEntity.getOtpId());
    }

    /**
     * Sends an OTP to the userMobile number.
     *
     * @param signUpMobileRequestDto
     *            the request containing userMobile details
     */
    @Async
    public void sendMobileOtp(SignUpMobileRequestDto signUpMobileRequestDto) {
        smsService.sendOtpSms(signUpMobileRequestDto.getUserMobile(), getOtpEntity(signUpMobileRequestDto.getUserId()),
                appOtpExpiryMinutes);
    }

    /**
     * Sends an OTP to the user's email address.
     *
     * @param signUpEmailRequestDto
     *            the request containing email details
     */
    @Async
    public void sendEmailOtp(SignUpEmailRequestDto signUpEmailRequestDto) {
        emailService.sendOtpEmail(signUpEmailRequestDto.getEmail(), getOtpEntity(signUpEmailRequestDto.getUserId()),
                appOtpExpiryMinutes);
    }

    /**
     * Sends an OTP to the user's email address.
     *
     * @param email
     *            the user's email address
     * @param userId
     *            the user ID for which the OTP is sent
     */
    @Async
    public void sendEmailOtp(String email, UUID userId) {
        emailService.sendOtpEmail(email, getOtpEntity(userId), appOtpExpiryMinutes);
    }

    /**
     * Sends an OTP to the user's number.
     *
     * @param userMobile
     *            the userMobile number
     * @param userId
     *            the user ID for which the OTP is sent
     */
    @Async
    public void sendMobileOtp(String userMobile, UUID userId) {
        smsService.sendOtpSms(userMobile, getOtpEntity(userId), appOtpExpiryMinutes);
    }

    /**
     * Retrieves the latest OTP audit for the given user ID, cleans up old OTPs, and
     * returns the OTP number.
     *
     * @param userId
     *            the user ID for which the OTP is retrieved
     * @return the latest OTP number as a string
     */
    public String getOtpEntity(UUID userId) {
        List<OtpEntity> otpEntities = otpRepository.findAllByUserId(userId);
        if (!otpEntities.isEmpty()) {
            for (OtpEntity otp : otpEntities) {
                otpRepository.deleteById(otp.getOtpId());
            }
        }
        OtpEntity newOtp = OtpEntity.builder().userId(userId).build();
        return generateOtp(newOtp);
    }
}
