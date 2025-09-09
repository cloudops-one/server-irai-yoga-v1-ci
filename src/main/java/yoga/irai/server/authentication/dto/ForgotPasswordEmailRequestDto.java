package yoga.irai.server.authentication.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.converter.ToLowerCaseConverter;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordEmailRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 508478561281129437L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_BLANK)
    @Email(message = AppUtils.Constants.INVALID_EMAIL_FORMAT)
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    String userEmail;
}
