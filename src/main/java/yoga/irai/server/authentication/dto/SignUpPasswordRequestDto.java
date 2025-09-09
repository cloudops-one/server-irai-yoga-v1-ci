package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
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
public class SignUpPasswordRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 7151299540149354435L;

    @NotNull(message = AppUtils.Constants.USER_ID_BLANK)
    UUID userId;

    @PasswordValidate
    @NotBlank(message = AppUtils.Constants.PASSWORD_BLANK)
    String password;
}
