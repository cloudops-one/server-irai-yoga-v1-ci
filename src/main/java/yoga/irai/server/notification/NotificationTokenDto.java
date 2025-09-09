package yoga.irai.server.notification;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTokenDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -7858161351504212833L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TOKEN_BLANK)
    private String fcmToken;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_CODE_BLANK)
    private String deviceCode;
}
