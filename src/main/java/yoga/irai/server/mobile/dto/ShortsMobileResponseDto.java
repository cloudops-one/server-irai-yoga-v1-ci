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
public class ShortsMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3079021913804452630L;

    private UUID shortsId;
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
}
