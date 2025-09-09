package yoga.irai.server.authentication.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.converter.ToLowerCaseConverter;
import yoga.irai.server.app.dto.AddressDto;

@Data
@Entity
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -7926013021227667007L;

    @Transient
    @Builder.Default
    private boolean skipAudit = false;

    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "user_icon_storage_id", nullable = false)
    private UUID userIconStorageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private AppUtils.UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private AppUtils.UserType userType;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "is_mobile_verified", nullable = false)
    private boolean isMobileVerified;

    @Column(name = "user_first_name", length = 100, nullable = false)
    private String userFirstName;

    @Column(name = "user_last_name", length = 100, nullable = false)
    private String userLastName;

    @Column(name = "user_email", nullable = false)
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String userEmail;

    @Column(name = "user_mobile", length = 35, nullable = false)
    private String userMobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private AppUtils.Gender gender;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addresses", columnDefinition = "jsonb")
    private List<AddressDto> addresses;

    @Column(name = "date_of_birth", columnDefinition = "DATE")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 10, nullable = false)
    private AppUtils.BloodGroup bloodGroup;

    @Column(name = "user_aoi", nullable = false)
    private String userAoi;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime createdAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime updatedAt;

    @Column(name = "last_login_at", columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        if (skipAudit) {
            return;
        }

        this.createdBy = AppUtils.getPrincipalUserId();
        this.updatedBy = AppUtils.getPrincipalUserId();
        this.isEmailVerified = false;
        this.isMobileVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        if (skipAudit) {
            return;
        }
        this.updatedBy = AppUtils.getPrincipalUserId();
    }
}
