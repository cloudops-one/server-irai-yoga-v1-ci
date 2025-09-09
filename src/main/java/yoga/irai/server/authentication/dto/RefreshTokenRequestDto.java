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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 5423061459425024914L;

    @NotBlank(message = AppUtils.Constants.REFRESH_TOKEN_BLANK)
    private String refreshToken;
}
