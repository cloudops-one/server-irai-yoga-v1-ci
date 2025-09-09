package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.validator.PasswordValidate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1635432395202904416L;

    @NotBlank(message = AppUtils.Constants.PASSWORD_BLANK)
    private String oldPassword;

    @PasswordValidate
    @NotBlank(message = AppUtils.Constants.PASSWORD_BLANK)
    private String newPassword;
}
