package yoga.irai.server.authentication.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.validator.MobileValidate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@MobileValidate
public class SignUpMobileRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 422668236389122319L;

    @NotNull(message = AppUtils.Constants.USER_ID_BLANK)
    private UUID userId;

    private String userMobile;
}
