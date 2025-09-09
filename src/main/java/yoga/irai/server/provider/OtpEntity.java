package yoga.irai.server.provider;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "otp")
public class OtpEntity {

    @Id
    @GeneratedValue
    @Column(name = "otp_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID otpId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "otp_number", nullable = false)
    private String otpNumber;

    @Column(name = "expiry_time", nullable = false)
    private ZonedDateTime expiryTime;
}
