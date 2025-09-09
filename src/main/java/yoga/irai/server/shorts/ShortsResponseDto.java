package yoga.irai.server.shorts;

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
public class ShortsResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3079021913804452630L;

    private UUID shortsId;
    private UUID orgId;
    private UUID shortsStorageId;
    private UUID shortsBannerStorageId;
    private AppUtils.ShortsStatus shortsStatus;
    private String orgIconStorageUrl;
    private String shortsStorageUrl;
    private String shortsExternalUrl;
    private String shortsBannerStorageUrl;
    private String shortsBannerExternalUrl;
    private String shortsName;
    private String shortsDescription;
    private String orgName;
    private Long duration;
    private Long likes;
    private Long views;
    private Set<String> tags;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
