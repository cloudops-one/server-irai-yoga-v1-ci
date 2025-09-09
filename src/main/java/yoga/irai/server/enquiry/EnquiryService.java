package yoga.irai.server.enquiry;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.provider.EmailService;

/**
 * Service class for handling enquiries. It saves the enquiry request and sends
 * confirmation emails to the user and support team.
 */
@Service
@AllArgsConstructor
public class EnquiryService {

    private final EmailService emailService;
    private final EnquiryRepository enquiryRepository;

    /**
     * Saves the enquiry request and sends confirmation emails.
     *
     * @param enquiryRequestDto
     *            the enquiry request data transfer object
     */
    public void updateEnquiry(EnquiryRequestDto enquiryRequestDto) {
        EnquiryEntity enquiryEntity = AppUtils.map(enquiryRequestDto, EnquiryEntity.class);
        enquiryRepository.save(enquiryEntity);
        emailService.sendMail(enquiryEntity.getEmail(), AppUtils.Constants.REPLY_MAIL_SUB,
                String.format(AppUtils.Constants.REPLY_MAIL_BODY, enquiryEntity.getName()));
        emailService.sendMail(AppUtils.Constants.EMAIL_FROM_ADDRESS, AppUtils.Constants.ENQUIRY_MAIL_TO_SUPPORT_SUB,
                String.format(AppUtils.Constants.ENQUIRY_MAIL_TO_SUPPORT_BODY, enquiryEntity.getName(),
                        enquiryEntity.getEmail(), enquiryEntity.getMessage()));
    }
}
