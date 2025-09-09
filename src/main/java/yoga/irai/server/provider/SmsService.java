package yoga.irai.server.provider;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;

/** Service for sending SMS messages using Twilio. */
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.from.number}")
    private String fromNumber;

    /**
     * Sends an OTP SMS to the specified phone number.
     *
     * @param to
     *            the recipient's phone number
     * @param otp
     *            the one-time password to send
     * @param expiryTime
     *            the expiry time of the OTP in minutes
     */
    public void sendOtpSms(String to, String otp, int expiryTime) {
        Twilio.init(accountSid, authToken);
        Message.creator(new com.twilio.type.PhoneNumber(to), new com.twilio.type.PhoneNumber(fromNumber),
                String.format(AppUtils.Constants.OTP_MESSAGE_1_OTP_2_EXPIRY_TIME, otp, expiryTime) // OTP message format
        ).create();
    }
}
