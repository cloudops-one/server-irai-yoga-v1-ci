package yoga.irai.server.authentication.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 3894586269271844780L;

    UUID userId;
}
