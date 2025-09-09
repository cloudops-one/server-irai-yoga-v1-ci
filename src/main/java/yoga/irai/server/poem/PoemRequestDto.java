package yoga.irai.server.poem;

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
public class PoemRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -892510854610063695L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String poemName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String poemDescription;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgId;

    private UUID poemStorageId;
    private UUID poemIconStorageId;
    private UUID poemBannerStorageId;
    private String poemExternalUrl;
    private String poemIconExternalUrl;
    private String poemBannerExternalUrl;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_TEXT_BLANK)
    private String poemText;

    private String poemAuthor;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TIME_BLANK)
    private Long poemDuration;

    private Set<String> poemTags;
}
