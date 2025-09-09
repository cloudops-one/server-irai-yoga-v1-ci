package yoga.irai.server.authentication.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserAoiDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8399373675896298274L;

    private int questionId;
    private String questionName;
    private AppUtils.OptionType optionType;
    private AppUtils.UserAoiStatus status;
    private List<UserAoiOptionDto> options;
    private int max;
}
