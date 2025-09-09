package yoga.irai.server.program.section.lesson;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LessonResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4081992683753416372L;

    private UUID lessonId;
    private UUID sectionId;
    private String sectionName;
    private String lessonName;
    private Integer lessonOrder;
    private UUID lessonStorageId;
    private String lessonStorageUrl;
    private String lessonExternalUrl;
    private Long duration;
    private String lessonDescription;
    private String lessonText;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
