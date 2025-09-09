package yoga.irai.server.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8485880944890520637L;

    private UUID orgId;
    private UUID orgIconStorageId;
    private String orgIconStorageUrl;
    private AppUtils.OrganizationStatus orgStatus;
    private String orgName;
    private String orgRegistrationNumber;
    private String orgEmail;
    private String orgDescription;
    private List<AddressDto> addresses;
    private List<ContactDto> contacts;
    private List<UrlDto> urls;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountType;
    private String bankIdentifierCode;
    private String bankBranch;
    private String bankAddress;
    private String bankCurrency;
    private String taxIdentificationNumber;
    private String permanentAccountNumber;
    private String goodsServicesTaxNumber;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
