package yoga.irai.server.program.user;

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
@Table(name = "program_user")
public class ProgramUserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4602825474926900287L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_user_id", nullable = false)
    private UUID programUserId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_user_status", nullable = false)
    private AppUtils.ProgramUserStatus programUserStatus;

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
        this.programUserStatus = AppUtils.ProgramUserStatus.STARTED;
    }
}
