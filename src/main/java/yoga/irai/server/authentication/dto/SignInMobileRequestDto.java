package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.validator.MobileValidate;
import yoga.irai.server.app.validator.PasswordValidate;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MobileValidate
public class SignInMobileRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1953995588172649696L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_MOBILE_BLANK)
    private String userMobile;

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
