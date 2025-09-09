package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import yoga.irai.server.app.AppUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignOutRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -9006020258338813338L;

    @NotBlank(message = AppUtils.Constants.USER_ID_BLANK)
    String userId;
}
