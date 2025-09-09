package yoga.irai.server.practice;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PracticeResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8676209729210674697L;

    private UUID practiceId;
    private UUID orgId;
    private UUID practiceCategoryId;
    private UUID practiceIconStorageId;
    private UUID practiceBannerStorageId;
    private UUID practiceStorageId;
    private AppUtils.PracticeStatus practiceStatus;
    private String orgIconStorageUrl;
    private String practiceIconStorageUrl;
    private String practiceIconExternalUrl;
    private String practiceBannerStorageUrl;
    private String practiceBannerExternalUrl;
    private String practiceStorageUrl;
    private String practiceExternalUrl;
    private String orgName;
    private String practiceCategoryName;
    private String practiceName;
    private String practiceDescription;
    private Long duration;
    private Set<String> tags;
    private float rating;
    private long ratingCount;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
