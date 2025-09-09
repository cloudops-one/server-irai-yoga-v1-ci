package yoga.irai.server.organization;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.converter.ToLowerCaseConverter;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4817888774912061707L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgIconStorageId;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String orgName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_REGISTRATION_BLANK)
    private String orgRegistrationNumber;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_BLANK)
    @Email(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_FORMAT_INVALID)
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String orgEmail;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_DESCRIPTION_BLANK)
    private String orgDescription;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_ADDRESSES_BLANK)
    private List<AddressDto> addresses;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_CONTACTS_BLANK)
    private List<ContactDto> contacts;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_URLS_BLANK)
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
}
