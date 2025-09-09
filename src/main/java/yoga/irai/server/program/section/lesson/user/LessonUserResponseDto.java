package yoga.irai.server.program.section.lesson.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LessonUserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -1146336965686634511L;

    private UUID lessonUserId;
    private UUID userId;
    private String userName;
    private UUID lessonId;
    private String lessonName;
    private AppUtils.LessonUserStatus lessonUserStatus;
    private Float rating;
    private String comments;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
