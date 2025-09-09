package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.validator.MobileValidate;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MobileValidate
public class ForgotPasswordMobileRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 508478561281129437L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_MOBILE_BLANK)
    String userMobile;
}
