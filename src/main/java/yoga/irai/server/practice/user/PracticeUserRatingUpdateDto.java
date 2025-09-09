package yoga.irai.server.practice.user;

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
public class PracticeUserRatingUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 7457534176783521823L;

    private String comment;

    @Max(message = AppUtils.Constants.VALIDATION_RATING_MIN_0_MAX_5, value = 5)
    @Min(message = AppUtils.Constants.VALIDATION_RATING_MIN_0_MAX_5, value = 0)
    private Float rating;
}
