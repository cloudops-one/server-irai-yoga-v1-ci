package yoga.irai.server.program.section.lesson;

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
public class LessonRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 3884403353995729465L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID sectionId;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String lessonName;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TIME_BLANK)
    private Long duration;

    private UUID lessonStorageId;
    private String lessonExternalUrl;
    private String lessonDescription;
    private String lessonText;
}
