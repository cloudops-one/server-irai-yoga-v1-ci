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
public class SignInResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4679487891903103689L;

    private String accessToken;
    private String refreshToken;
}
