package yoga.irai.server.program;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProgramRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2661502655989746918L;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String programName;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgId;

    private UUID programBannerStorageId;

    private String programBannerExternalUrl;

    private String programDescription;

    private String programAuthor;

    private AppUtils.ProgramFlag flag;

    private Set<String> tags;

}
