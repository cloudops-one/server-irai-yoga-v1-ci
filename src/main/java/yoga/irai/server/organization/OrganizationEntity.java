package yoga.irai.server.organization;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.audit.Auditable;
import yoga.irai.server.app.converter.Base64Converter;
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
@Table(name = "organizations")
public class OrganizationEntity extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = -3971120252895573304L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "org_icon_storage_id")
    private UUID orgIconStorageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_status", nullable = false, length = 20)
    private AppUtils.OrganizationStatus orgStatus;

    @Column(name = "org_name", nullable = false, unique = true)
    private String orgName;

    @Column(name = "org_registration_number", nullable = false, length = 100)
    private String orgRegistrationNumber;

    @Column(name = "org_email", nullable = false)
    private String orgEmail;

    @Column(name = "org_description", columnDefinition = "TEXT")
    private String orgDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addresses", columnDefinition = "jsonb")
    private List<AddressDto> addresses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contacts", columnDefinition = "jsonb")
    private List<ContactDto> contacts;

    @Column(name = "urls", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<UrlDto> urls;

    @Column(name = "bank_name")
    private String bankName;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_account_type")
    private String bankAccountType;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_identifier_code")
    private String bankIdentifierCode;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_branch")
    private String bankBranch;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_address")
    private String bankAddress;

    @Convert(converter = Base64Converter.class)
    @Column(name = "bank_currency")
    private String bankCurrency;

    @Convert(converter = Base64Converter.class)
    @Column(name = "tax_identification_number")
    private String taxIdentificationNumber;

    @Convert(converter = Base64Converter.class)
    @Column(name = "permanent_account_number")
    private String permanentAccountNumber;

    @Convert(converter = Base64Converter.class)
    @Column(name = "goods_services_tax_number")
    private String goodsServicesTaxNumber;

    @PrePersist
    protected void onCreate() {
        this.orgStatus = AppUtils.OrganizationStatus.INACTIVE;
    }
}
