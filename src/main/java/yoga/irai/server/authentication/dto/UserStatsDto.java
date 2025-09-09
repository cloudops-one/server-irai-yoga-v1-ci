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
public class UserStatsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6032858515630304248L;

    private Long totalUser;
    private Long totalActiveUsers;
    private Long totalMobileUsers;
    private Long totalActiveMobileUsers;
}
