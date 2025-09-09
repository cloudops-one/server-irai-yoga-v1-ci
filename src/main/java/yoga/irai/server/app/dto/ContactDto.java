package yoga.irai.server.app.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.converter.ToLowerCaseConverter;
import yoga.irai.server.app.validator.MobileValidate;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@MobileValidate()
public class ContactDto implements AppUtils.HasId, AppUtils.HasPrimary, Serializable {

    @Serial
    private static final long serialVersionUID = 659636143423412L;

    private Integer id;

    @Builder.Default
    private Boolean isPrimary = false;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String name;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_MOBILE_BLANK)
    private String mobile;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_EMAIL_BLANK)
    @JsonDeserialize(converter = ToLowerCaseConverter.class)
    private String email;
}
