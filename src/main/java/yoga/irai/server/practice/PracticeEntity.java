package yoga.irai.server.practice;

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
@Table(name = "practice")
public class PracticeEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -6317657294653222644L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_id", nullable = false)
    private UUID practiceId;

    @Column(name = "practice_name", columnDefinition = "text", nullable = false, unique = true)
    private String practiceName;

    @Column(name = "practice_description", columnDefinition = "text")
    private String practiceDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_status", nullable = false, length = 20)
    private AppUtils.PracticeStatus practiceStatus;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "practice_category_id", nullable = false)
    private UUID practiceCategoryId;

    @Column(name = "practice_icon_storage_id")
    private UUID practiceIconStorageId;

    @Column(name = "practice_icon_external_url", columnDefinition = "text")
    private String practiceIconExternalUrl;

    @Column(name = "practice_banner_storage_id")
    private UUID practiceBannerStorageId;

    @Column(name = "practice_banner_external_url", columnDefinition = "text")
    private String practiceBannerExternalUrl;

    @Column(name = "practice_storage_id")
    private UUID practiceStorageId;

    @Column(name = "practice_external_url", columnDefinition = "text")
    private String practiceExternalUrl;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "tags")
    private String tags;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "rating_count")
    private Long ratingCount;

    @PrePersist
    protected void onCreate() {
        this.practiceStatus = AppUtils.PracticeStatus.INACTIVE;
    }
}
