package yoga.irai.server.app.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TotalDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 8944016288770797031L;

    private Long total;
}
