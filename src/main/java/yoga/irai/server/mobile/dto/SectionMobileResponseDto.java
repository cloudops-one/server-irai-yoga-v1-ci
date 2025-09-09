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
public class SectionMobileResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 6722969884728723280L;

    private UUID sectionId;
    private String sectionName;
    private String sectionDescription;
    private int numberOfLessons;
    private int sectionOrder;
}
