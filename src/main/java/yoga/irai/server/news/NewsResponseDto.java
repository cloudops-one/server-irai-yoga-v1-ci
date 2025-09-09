package yoga.irai.server.news;

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
public class NewsResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 4983419446323511779L;

    private UUID newsId;
    private UUID newsIconStorageId;
    private UUID newsBannerStorageId;
    private String newsIconExternalUrl;
    private String newsIconStorageUrl;
    private String newsBannerExternalUrl;
    private String newsBannerStorageUrl;
    private AppUtils.NewsStatus newsStatus;
    private String newsName;
    private String newsDescription;
    private Boolean isRecommended;
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
