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
import yoga.irai.server.app.AppUtils.RefreshTokenStatus;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8829459980664969559L;

    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID refreshTokenId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private DeviceEntity deviceEntity;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ", nullable = false)
    private ZonedDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "refresh_token_status", length = 20, nullable = false)
    private RefreshTokenStatus refreshTokenStatus; // Enum suggested in real apps

    @Builder.Default
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
