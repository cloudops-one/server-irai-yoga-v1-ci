package yoga.irai.server.poem;

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
@Table(name = "poem")
public class PoemEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -7312340797509652097L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "poem_id", nullable = false)
    private UUID poemId;

    @Column(name = "poem_name", nullable = false, unique = true)
    private String poemName;

    @Column(name = "poem_description", columnDefinition = "TEXT")
    private String poemDescription;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "poem_storage_id")
    private UUID poemStorageId;

    @Column(name = "poem_external_url")
    private String poemExternalUrl;

    @Column(name = "poem_icon_storage_id")
    private UUID poemIconStorageId;

    @Column(name = "poem_icon_external_url")
    private String poemIconExternalUrl;

    @Column(name = "poem_banner_storage_id")
    private UUID poemBannerStorageId;

    @Column(name = "poem_banner_external_url")
    private String poemBannerExternalUrl;

    @Column(name = "poem_text", columnDefinition = "TEXT")
    private String poemText;

    @Column(name = "poem_author")
    private String poemAuthor;

    @Enumerated(EnumType.STRING)
    @Column(name = "poem_status", nullable = false, length = 20)
    private AppUtils.PoemStatus poemStatus;

    @Column(name = "poem_duration")
    private Long poemDuration;

    @Builder.Default
    @Column(name = "poem_views")
    private Long poemViews = 0L;

    @Column(name = "poem_tags")
    private String poemTags;

    @PrePersist
    protected void onCreate() {
        this.poemStatus = AppUtils.PoemStatus.INACTIVE;
    }
}
