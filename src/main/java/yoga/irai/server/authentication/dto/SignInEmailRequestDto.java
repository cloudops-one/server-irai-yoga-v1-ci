package yoga.irai.server.authentication.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.converter.ToLowerCaseConverter;
import yoga.irai.server.app.validator.PasswordValidate;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SignInEmailRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1953995588172649696L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_BLANK)
    @Email(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_FORMAT_INVALID)
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String userEmail;

    @NotBlank(message = AppUtils.Constants.PASSWORD_BLANK)
    @PasswordValidate
    private String password;

    @NotBlank(message = AppUtils.Constants.DEVICE_CODE_BLANK)
    private String deviceCode;

    @NotBlank(message = AppUtils.Constants.DEVICE_NAME_BLANK)
    private String deviceName;

    @NotBlank(message = AppUtils.Constants.DEVICE_TYPE_BLANK)
    private String deviceType;
}
