package yoga.irai.server.enquiry;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "enquiry")
public class EnquiryEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1419874259993361179L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enquiry_id", nullable = false)
    private UUID enquiryId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime createdAt;
}
