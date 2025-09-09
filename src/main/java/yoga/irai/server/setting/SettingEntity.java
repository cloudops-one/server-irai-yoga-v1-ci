package yoga.irai.server.setting;

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
@Table(name = "setting")
public class SettingEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1297818808449833813L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id", nullable = false)
    private UUID settingId;

    @Column(name = "setting_name", unique = true, nullable = false)
    private String settingName;

    @Column(name = "setting_value", columnDefinition = "TEXT", nullable = false)
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_status")
    private AppUtils.SettingStatus settingStatus;

    @PrePersist
    protected void onCreate() {
        this.settingStatus = AppUtils.SettingStatus.INACTIVE;
    }
}
