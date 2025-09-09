package yoga.irai.server.program.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import yoga.irai.server.app.AppUtils;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProgramUserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4177497324530526699L;

    private UUID programUserId;
    private UUID userId;
    private String userName;
    private UUID programId;
    private String programName;
    private AppUtils.ProgramUserStatus programUserStatus;
    private Float rating;
    private String comments;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
