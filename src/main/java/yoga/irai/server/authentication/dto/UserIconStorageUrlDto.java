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
public class UserIconStorageUrlDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6352274255965299571L;

    private String userIconStorageUrl;
}
