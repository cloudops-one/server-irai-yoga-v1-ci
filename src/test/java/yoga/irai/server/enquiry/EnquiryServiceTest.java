package yoga.irai.server.enquiry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.provider.EmailService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EnquiryServiceTest {
    @InjectMocks
    EnquiryService enquiryService;
    @Mock
    EnquiryRepository enquiryRepository;
    @Mock
    EmailService  emailService;
    private EnquiryRequestDto  enquiryRequestDto;
    private EnquiryEntity enquiryEntity;
    @BeforeEach
    void setUp() {
        UUID enquiryId = UUID.randomUUID();
        enquiryRequestDto = EnquiryRequestDto.builder()
                .name("Hilton")
                .email("hilton.p@terv.pro")
                .message("Test")
                .build();
        enquiryEntity = EnquiryEntity.builder()
                .enquiryId(enquiryId)
                .name("Hilton")
                .email("hilton.p@terv.pro")
                .message("Test")
                .createdAt(ZonedDateTime.of(2025, 7, 14, 12, 30, 0, 0, ZoneId.of("Asia/Kolkata")))
                .build();
    }
    @Test
    void updateEnquiryTest(){
        try (MockedStatic<AppUtils> appUtilsMockedStatic = Mockito.mockStatic(AppUtils.class)) {
            appUtilsMockedStatic.when(() ->AppUtils.map(enquiryRequestDto , EnquiryEntity.class))
                    .thenReturn(enquiryEntity);
            enquiryService.updateEnquiry(enquiryRequestDto);
            verify(enquiryRepository, times(1)).save(enquiryEntity);
            verify(emailService, times(1)).sendMail(
                    eq(enquiryEntity.getEmail()),
                    eq(AppUtils.Constants.REPLY_MAIL_SUB),
                    contains(enquiryEntity.getName())
            );
            verify(emailService, times(1)).sendMail(
                    eq(AppUtils.Constants.EMAIL_FROM_ADDRESS),
                    eq(AppUtils.Constants.ENQUIRY_MAIL_TO_SUPPORT_SUB),
                    contains(enquiryEntity.getMessage())
            );
        }
    }
}
