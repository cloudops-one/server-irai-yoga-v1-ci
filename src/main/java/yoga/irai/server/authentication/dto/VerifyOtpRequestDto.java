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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyOtpRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 279244808934750556L;

    @NotNull(message = AppUtils.Constants.USER_ID_BLANK)
    private UUID userId;

    @NotBlank(message = AppUtils.Constants.OTP_REQUIRED)
    private String otp;
}
