package yoga.irai.server.program.section.lesson;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.audit.Auditable;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "program_lesson")
public class LessonEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -4049331750272125301L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "lesson_order")
    private Integer lessonOrder;

    @Column(name = "lesson_name")
    private String lessonName;

    @Column(name = "lesson_storage_id")
    private UUID lessonStorageId;

    @Column(name = "lesson_external_url")
    private String lessonExternalUrl;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "lesson_description", columnDefinition = "text")
    private String lessonDescription;

    @Column(name = "lesson_text", columnDefinition = "text")
    private String lessonText;
}
