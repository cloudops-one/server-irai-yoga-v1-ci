package yoga.irai.server.program.section.lesson.user;

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
public class LessonUserRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3205638234558751887L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID userId;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID lessonId;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TIME_BLANK)
    private Long resumeTime;
}
