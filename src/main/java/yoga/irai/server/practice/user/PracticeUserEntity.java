package yoga.irai.server.practice.user;

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
@Table(name = "practice_user")
public class PracticeUserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6644432686146849298L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_user_id", nullable = false)
    private UUID practiceUserId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "practice_id", nullable = false)
    private UUID practiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_user_status", nullable = false)
    private AppUtils.PracticeUserStatus practiceUserStatus;

    @Column(name = "resume_time", columnDefinition = "bigint")
    private Long resumeTime;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "comments", length = 500)
    private String comments;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.practiceUserStatus = AppUtils.PracticeUserStatus.STARTED;
    }
}
