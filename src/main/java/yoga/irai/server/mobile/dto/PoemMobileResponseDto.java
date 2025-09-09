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
public class PoemMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -1475571210367928280L;

    private UUID poemId;
    private String poemStorageUrl;
    private String poemIconStorageUrl;
    private String poemBannerStorageUrl;
    private String poemExternalUrl;
    private String poemIconExternalUrl;
    private String poemBannerExternalUrl;
    private String poemName;
    private String poemDescription;
    private String poemText;
    private String poemAuthor;
    private Long poemDuration;
    private Long poemViews;
    private Set<String> poemTags;
}
