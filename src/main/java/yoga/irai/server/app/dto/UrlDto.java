package yoga.irai.server.app.dto;

import jakarta.validation.constraints.NotBlank;
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
public class UrlDto implements AppUtils.HasId, Serializable {

    @Serial
    private static final long serialVersionUID = 23484323473466234L;

    private Integer id;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String url;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TYPE_BLANK)
    private AppUtils.UrlType type;
}
