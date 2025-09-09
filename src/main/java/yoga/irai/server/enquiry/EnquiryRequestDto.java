package yoga.irai.server.enquiry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 3357665661265096395L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String name;

    @Email(message = AppUtils.Constants.INVALID_EMAIL_FORMAT)
    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_BLANK)
    private String email;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_MESSAGE_BLANK)
    private String message;

}
