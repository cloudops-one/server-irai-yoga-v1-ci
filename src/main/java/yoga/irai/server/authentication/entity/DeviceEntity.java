package yoga.irai.server.authentication.entity;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 192694856176247098L;

    @Id
    @GeneratedValue
    @Column(name = "device_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID deviceId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "device_code")
    private String deviceCode;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    // Twilio Notify
    @Column(name = "twilio_binding_sid", length = 34)
    private String twilioBindingSid;

    @Column(name = "twilio_push_type", length = 10)
    private String twilioPushType;

    // Twilio SMS/WhatsApp
    @Column(name = "twilio_sms_number", length = 20)
    private String twilioSmsNumber;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Builder.Default
    @Column(name = "accepts_sms", nullable = false)
    private boolean acceptsSms = false;

    @Builder.Default
    @Column(name = "accepts_whatsapp", nullable = false)
    private boolean acceptsWhatsapp = false;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "last_active", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime lastActive;

    @Builder.Default
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
