package yoga.irai.server.shorts.user;

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
@Table(name = "shorts_user")
public class ShortsUserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 3298607038873381089L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shorts_user_id", nullable = false)
    private UUID shortsUserId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "shorts_id", nullable = false)
    private UUID shortsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shorts_user_status", nullable = false)
    private AppUtils.ShortsUserStatus shortsUserStatus;

    @Builder.Default
    @Column(name = "likes")
    private Boolean likes = false;

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
        this.shortsUserStatus = AppUtils.ShortsUserStatus.NEW;
    }
}
