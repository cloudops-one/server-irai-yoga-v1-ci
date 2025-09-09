package yoga.irai.server.storage;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import yoga.irai.server.app.AppUtils;

@Data
@Entity
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "storage")
public class StorageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8035995315304496687L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_id")
    private UUID storageId;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "extension", nullable = false)
    private String extension;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "tags")
    private String tags;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdBy = AppUtils.getPrincipalUserId();
    }
}
