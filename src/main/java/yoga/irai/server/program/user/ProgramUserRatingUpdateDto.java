package yoga.irai.server.program.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class ProgramUserRatingUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8586378282603948607L;

    private String comment;

    @Max(message = AppUtils.Constants.VALIDATION_RATING_MIN_0_MAX_5, value = 5)
    @Min(message = AppUtils.Constants.VALIDATION_RATING_MIN_0_MAX_5, value = 0)
    private Float rating;
}
