package yoga.irai.server.practice.category;

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
@Table(name = "practice_category")
public class PracticeCategoryEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -792256258120480557L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "practice_category_id")
    private UUID practiceCategoryId;

    @Column(name = "practice_category_icon_storage_id")
    private UUID practiceCategoryIconStorageId;

    @Column(name = "practice_category_icon_external_url")
    private String practiceCategoryIconExternalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_category_status")
    private AppUtils.PracticeCategoryStatus practiceCategoryStatus;

    @Column(name = "practice_category_name", unique = true, nullable = false)
    private String practiceCategoryName;

    @PrePersist
    protected void onCreate() {
        this.practiceCategoryStatus = AppUtils.PracticeCategoryStatus.INACTIVE;
    }
}
