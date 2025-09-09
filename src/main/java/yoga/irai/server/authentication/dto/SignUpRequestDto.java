package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yoga.irai.server.app.AppUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1618466821108810147L;

    @NotBlank(message = AppUtils.Constants.USER_FIRST_NAME_BLANK)
    String firstName;

    @NotBlank(message = AppUtils.Constants.USER_LAST_NAME_BLANK)
    String lastName;
}
