package yoga.irai.server.setting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SettingRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 4623952586182065230L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String settingName;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_LIST_BLANK)
    private transient List<Object> settingValue;
}
