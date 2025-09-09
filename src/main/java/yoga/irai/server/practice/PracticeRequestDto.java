package yoga.irai.server.practice;

import jakarta.validation.constraints.NotBlank;
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
public class PracticeRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8257064639003880730L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String practiceName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String practiceDescription;

    private UUID orgId;
    private UUID practiceCategoryId;
    private UUID practiceIconStorageId;
    private String practiceIconExternalUrl;
    private UUID practiceBannerStorageId;
    private String practiceBannerExternalUrl;
    private UUID practiceStorageId;
    private String practiceExternalUrl;
    private Long duration;
    private Set<String> tags;
}
