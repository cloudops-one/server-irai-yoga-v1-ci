package yoga.irai.server.authentication.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AddressDto;
import yoga.irai.server.app.validator.MobileValidate;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MobileValidate
public class UserRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1754253204364379676L;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_ID_BLANK)
    private UUID orgId;

    private UUID userIconStorageId;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String userFirstName;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String userLastName;

    @Email(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_FORMAT_INVALID)
    private String userEmail;

    private String userMobile;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_GENDER_BLANK)
    private AppUtils.Gender gender;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_DATE_BLANK)
    private Date dateOfBirth;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_BLOOD_GROUP_BLANK)
    private AppUtils.BloodGroup bloodGroup;

    @Valid
    @NotEmpty(message = AppUtils.Constants.VALIDATION_FAILED_ADDRESSES_BLANK)
    private List<AddressDto> addresses;
}
