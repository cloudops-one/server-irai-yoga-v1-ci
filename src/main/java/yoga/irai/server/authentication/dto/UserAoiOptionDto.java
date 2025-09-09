package yoga.irai.server.authentication.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserAoiOptionDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 3535837717619138138L;

    private int id;
    private String value;
    private boolean isSelected;
}
