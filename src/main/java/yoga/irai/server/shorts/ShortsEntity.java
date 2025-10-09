package yoga.irai.server.shorts;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.audit.Auditable;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shorts")
public class ShortsEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 5773616810895049585L;

    @Id
    @GeneratedValue
    @Column(name = "shorts_id", nullable = false)
    private UUID shortsId;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "shorts_storage_id")
    private UUID shortsStorageId;

    @Column(name = "shorts_banner_storage_id")
    private UUID shortsBannerStorageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shorts_status", nullable = false, length = 20)
    private AppUtils.ShortsStatus shortsStatus;

    @Column(name = "shorts_external_url")
    private String shortsExternalUrl;

    @Column(name = "shorts_banner_external_url")
    private String shortsBannerExternalUrl;

    @Column(name = "shorts_name", unique = true, nullable = false)
    private String shortsName;

    @Column(name = "shorts_description", columnDefinition = "TEXT")
    private String shortsDescription;

    @Column(name = "duration")
    private Long duration;

    @Builder.Default
    @Column(name = "likes")
    private Long likes = 0L;

    @Builder.Default
    @Column(name = "views")
    private Long views = 0L;

    @Column(name = "tags")
    private String tags;

    @PrePersist
    protected void onCreate() {
        this.shortsStatus = AppUtils.ShortsStatus.INACTIVE;
    }
}
