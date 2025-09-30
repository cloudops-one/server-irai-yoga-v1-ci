package yoga.irai.server.provider;

import com.twilio.rest.api.v2010.account.MessageCreator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yoga.irai.server.app.AppUtils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsService smsService;

    @Test
    void testSendOtpSms() {
        // Inject private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(smsService, "accountSid", "testSid");
        ReflectionTestUtils.setField(smsService, "authToken", "testToken");
        ReflectionTestUtils.setField(smsService, "fromNumber", "+1234567890");

        String to = "+19876543210";
        String otp = "123456";
        int expiryTime = 5;

        try (MockedStatic<Twilio> twilioMock = mockStatic(Twilio.class);
            MockedStatic<Message> messageMock = mockStatic(Message.class)) {
            MessageCreator messageCreatorMock = mock(MessageCreator.class);
            messageMock.when(() ->
                    Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())
            ).thenReturn(messageCreatorMock);
            when(messageCreatorMock.create()).thenReturn(mock(Message.class));
            smsService.sendOtpSms(to, otp, expiryTime);
            twilioMock.verify(() -> Twilio.init(anyString(), any()));
            messageMock.verify(() ->
                    Message.creator(
                            new PhoneNumber(to),
                            new PhoneNumber("+1234567890"),
                            String.format(AppUtils.Constants.OTP_MESSAGE_1_OTP_2_EXPIRY_TIME, otp, expiryTime)
                    )
            );
            verify(messageCreatorMock).create();
        }
    }
}
