package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.Email;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpEmailRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8454162950953791851L;

    @NotNull(message = AppUtils.Constants.USER_ID_BLANK)
    private UUID userId;

    @NotBlank(message = AppUtils.Constants.EMAIL_BLANK)
    @Email(message = AppUtils.Constants.INVALID_EMAIL_FORMAT)
    private String email;
}
