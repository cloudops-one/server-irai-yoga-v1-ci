package yoga.irai.server.authentication.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 508478561281129437L;

    private UUID userId;
}
