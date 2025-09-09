package yoga.irai.server.program.section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SectionRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4745432643877725314L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID programId;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String sectionName;

    private String sectionDescription;
}
