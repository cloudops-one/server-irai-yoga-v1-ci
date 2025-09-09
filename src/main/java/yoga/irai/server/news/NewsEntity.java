package yoga.irai.server.news;

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
@Table(name = "news")
public class NewsEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1134572822285589649L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id", nullable = false)
    private UUID newsId;

    @Column(name = "news_icon_storage_id")
    private UUID newsIconStorageId;

    @Column(name = "news_banner_storage_id")
    private UUID newsBannerStorageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_status", nullable = false, length = 20)
    private AppUtils.NewsStatus newsStatus;

    @Column(name = "news_icon_external_url")
    private String newsIconExternalUrl;

    @Column(name = "news_banner_external_url")
    private String newsBannerExternalUrl;

    @Builder.Default
    @Column(name = "is_recommended")
    private Boolean isRecommended = false;

    @Column(name = "news_name")
    private String newsName;

    @Column(name = "news_description", columnDefinition = "text")
    private String newsDescription;

    @Builder.Default
    @Column(name = "likes")
    private Long likes = 0L;

    @Builder.Default
    @Column(name = "views")
    private Long views = 0L;

    @Column(name = "tags")
    private String tags;

    @PrePersist
    public void onCreate() {
        this.newsStatus = AppUtils.NewsStatus.ACTIVE;
    }
}
