package yoga.irai.server.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LessonMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4081992683753416372L;

    private UUID lessonId;
    private String lessonName;
    private Integer lessonOrder;
    private String lessonStorageUrl;
    private String lessonExternalUrl;
    private Long duration;
    private String lessonDescription;
    private String lessonText;
    private UUID lessonUserId;
    private Long resumeTime;
    private AppUtils.LessonUserStatus lessonUserStatus;
}
