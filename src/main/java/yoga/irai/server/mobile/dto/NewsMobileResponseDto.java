package yoga.irai.server.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NewsMobileResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 4983419446323511779L;

    private UUID newsId;
    private String newsIconExternalUrl;
    private String newsIconStorageUrl;
    private String newsBannerExternalUrl;
    private String newsBannerStorageUrl;
    private String newsName;
    private String newsDescription;
    private Boolean isRecommended;
    private Long likes;
    private Long views;
    private Set<String> tags;
}
