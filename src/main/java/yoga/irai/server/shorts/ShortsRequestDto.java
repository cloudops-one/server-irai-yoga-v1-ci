package yoga.irai.server.shorts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ShortsRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3310131970796304226L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgId;

    private UUID shortsStorageId;
    private UUID shortsBannerStorageId;
    private String shortsExternalUrl;
    private String shortsBannerExternalUrl;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String shortsName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String shortsDescription;

    private Set<String> tags;
    private Long duration;
}
