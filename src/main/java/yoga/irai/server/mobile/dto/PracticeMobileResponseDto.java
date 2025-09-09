package yoga.irai.server.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PracticeMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8676209729210674697L;

    private UUID practiceId;
    private String practiceIconStorageUrl;
    private String practiceIconExternalUrl;
    private String practiceBannerStorageUrl;
    private String practiceBannerExternalUrl;
    private String practiceStorageUrl;
    private String practiceExternalUrl;
    private String practiceCategoryName;
    private String practiceName;
    private String practiceDescription;
    private Long duration;
    private Set<String> tags;
    private float rating;
    private long ratingCount;
    private UUID practiceUserId;
    private AppUtils.PracticeUserStatus practiceUserStatus;
    private Long resumeTime;
}
