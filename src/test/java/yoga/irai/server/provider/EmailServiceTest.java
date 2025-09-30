package yoga.irai.server.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendMail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendMail("to@email.com","sub","body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendOtpEmail(){
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendOtpEmail("to@email.com","999999",5);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

}
