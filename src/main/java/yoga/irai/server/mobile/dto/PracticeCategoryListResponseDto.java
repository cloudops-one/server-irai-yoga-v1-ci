package yoga.irai.server.mobile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PracticeCategoryListResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -1646164651244316718L;

    private UUID practiceCategoryId;
    private String practiceCategoryName;
    private String practiceCategoryIconStorageUrl;
    private String practiceCategoryIconExternalUrl;
}
