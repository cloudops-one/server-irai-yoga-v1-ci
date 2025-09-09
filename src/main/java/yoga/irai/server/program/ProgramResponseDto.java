package yoga.irai.server.program;

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
public class ProgramResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 662235465530136449L;

    private UUID programId;
    private UUID orgId;
    private String orgName;
    private UUID programBannerStorageId;
    private String orgIconStorageUrl;
    private String programBannerStorageUrl;
    private String programBannerExternalUrl;
    private String programName;
    private String programDescription;
    private String programAuthor;
    private AppUtils.ProgramStatus programStatus;
    private Long duration;
    private Integer numberOfLessons;
    private Float rating;
    private Long ratingCount;
    private String comments;
    private Long views;
    private String flag;
    private Set<String> tags;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
