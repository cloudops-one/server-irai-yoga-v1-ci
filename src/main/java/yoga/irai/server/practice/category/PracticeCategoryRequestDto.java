package yoga.irai.server.practice.category;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PracticeCategoryRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2571980027827311574L;

    private UUID practiceCategoryIconStorageId;
    private String practiceCategoryIconExternalUrl;

    @NotBlank(message = AppUtils.Constants.VALIDATION_FAILED_NAME_BLANK)
    private String practiceCategoryName;
}
