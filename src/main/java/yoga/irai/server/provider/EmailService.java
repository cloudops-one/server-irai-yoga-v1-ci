package yoga.irai.server.provider;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;

/** Service for sending emails. */
@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an OTP email to the specified recipient.
     *
     * @param to
     *            the recipient's email address
     * @param otp
     *            the one-time password to be sent
     * @param expiryTime
     *            the time in minutes after which the OTP expires
     */
    public void sendOtpEmail(String to, String otp, int expiryTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(AppUtils.Constants.EMAIL_FROM_ADDRESS);
        message.setTo(to);
        message.setSubject(AppUtils.Constants.OTP_EMAIL_SUBJECT);
        String body = String.format(AppUtils.Constants.OTP_EMAIL_BODY, to, otp, expiryTime);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Sends a generic email with the specified subject and body.
     *
     * @param to
     *            the recipient's email address
     * @param subject
     *            the subject of the email
     * @param body
     *            the body of the email
     */
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(AppUtils.Constants.EMAIL_FROM_ADDRESS);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
