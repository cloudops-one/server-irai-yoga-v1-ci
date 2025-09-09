package yoga.irai.server.practice.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PracticeCategoryResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -8465064584697842514L;

    private UUID practiceCategoryId;
    private UUID practiceCategoryIconStorageId;
    private AppUtils.PracticeCategoryStatus practiceCategoryStatus;
    private String practiceCategoryIconStorageUrl;
    private String practiceCategoryIconExternalUrl;
    private String practiceCategoryName;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
