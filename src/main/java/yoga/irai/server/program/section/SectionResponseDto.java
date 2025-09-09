package yoga.irai.server.program.section;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SectionResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6722969884728723280L;

    private UUID sectionId;
    private UUID programId;
    private String programName;
    private String sectionName;
    private String sectionDescription;
    private int numberOfLessons;
    private int sectionOrder;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
