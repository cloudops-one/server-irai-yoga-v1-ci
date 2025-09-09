package yoga.irai.server.poem;

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
public class PoemResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -1475571210367928280L;

    private UUID poemId;
    private UUID orgId;
    private UUID poemStorageId;
    private UUID poemIconStorageId;
    private UUID poemBannerStorageId;
    private AppUtils.PoemStatus poemStatus;
    private String orgIconStorageUrl;
    private String poemStorageUrl;
    private String poemIconStorageUrl;
    private String poemBannerStorageUrl;
    private String poemExternalUrl;
    private String poemIconExternalUrl;
    private String poemBannerExternalUrl;
    private String orgName;
    private String poemName;
    private String poemDescription;
    private String poemText;
    private String poemAuthor;
    private Long poemDuration;
    private Long poemViews;
    private Set<String> poemTags;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
