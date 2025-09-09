package yoga.irai.server.program.section.lesson.user;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import yoga.irai.server.app.AppUtils;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "program_lesson_user")
public class LessonUserEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -3711535317040630312L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_user_id", nullable = false)
    private UUID lessonUserId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_user_status", nullable = false)
    private AppUtils.LessonUserStatus lessonUserStatus;

    @Column(name = "resume_time")
    private Long resumeTime;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.lessonUserStatus = AppUtils.LessonUserStatus.STARTED;
    }
}
