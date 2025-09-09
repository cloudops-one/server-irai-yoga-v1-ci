package yoga.irai.server.news;

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
public class NewsRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 4983419446323511779L;

    private UUID newsIconStorageId;
    private UUID newsBannerStorageId;
    private String newsIconExternalUrl;
    private String newsBannerExternalUrl;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String newsName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String newsDescription;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_RECOMMENDED_BLANK)
    private Boolean isRecommended;

    private Set<String> tags;
}
