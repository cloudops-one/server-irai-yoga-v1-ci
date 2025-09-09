package yoga.irai.server.program;

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
@Table(name = "program")
public class ProgramEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -2020177048281806189L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "program_banner_storage_id")
    private UUID programBannerStorageId;

    @Column(name = "program_banner_external_url")
    private String programBannerExternalUrl;

    @Column(name = "program_name", nullable = false, unique = true)
    private String programName;

    @Column(name = "program_description", columnDefinition = "text")
    private String programDescription;

    @Column(name = "program_author")
    private String programAuthor;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_status", nullable = false, length = 20)
    private AppUtils.ProgramStatus programStatus;

    @Builder.Default
    @Column(name = "duration")
    private Long duration = 0L;

    @Builder.Default
    @Column(name = "number_of_lessons", nullable = false)
    private Integer numberOfLessons = 0;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "rating_count")
    private Long ratingCount;

    @Column(name = "comments")
    private String comments;

    @Column(name = "views")
    private Long views;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag", length = 20)
    private AppUtils.ProgramFlag flag;

    @Column(name = "tags", columnDefinition = "text")
    private String tags;

    @PrePersist
    protected void onCreate() {
        this.programStatus = AppUtils.ProgramStatus.INACTIVE;
    }
}
