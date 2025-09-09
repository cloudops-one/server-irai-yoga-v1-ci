package yoga.irai.server.app.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements AppUtils.HasId, AppUtils.HasPrimary, Serializable {

    @Serial
    private static final long serialVersionUID = -2324442566788774L;

    private Integer id;

    @Builder.Default
    private Boolean isPrimary = false;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_ADDRESS_LINE_1_BLANK)
    private String addressLine1;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_ADDRESS_LINE_2_BLANK)
    private String addressLine2;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_CITY_BLANK)
    private String city;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_STATE_PROVINCE_BLANK)
    private String stateProvince;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_POSTAL_CODE_BLANK)
    private String postalCode;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_COUNTRY_BLANK)
    private String country;
}
