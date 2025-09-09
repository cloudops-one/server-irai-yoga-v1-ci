package yoga.irai.server.event;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.audit.Auditable;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class EventEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 350114973352097336L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "event_icon_storage_id")
    private UUID eventIconStorageId;

    @Column(name = "event_banner_storage_id")
    private UUID eventBannerStorageId;

    @Column(name = "event_icon_external_url")
    private String eventIconExternalUrl;

    @Column(name = "event_banner_external_url")
    private String eventBannerExternalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    private AppUtils.EventStatus eventStatus;

    @Column(name = "event_name", nullable = false, unique = true)
    private String eventName;

    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;

    @Column(name = "event_start_date_time", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime eventStartDateTime;

    @Column(name = "event_end_date_time", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime eventEndDateTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addresses", columnDefinition = "jsonb")
    private List<AddressDto> addresses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contacts", columnDefinition = "jsonb")
    private List<ContactDto> contacts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "urls", columnDefinition = "jsonb")
    private List<UrlDto> urls;

    @PrePersist
    protected void onCreate() {
        this.eventStatus = AppUtils.EventStatus.INACTIVE;
    }
}
