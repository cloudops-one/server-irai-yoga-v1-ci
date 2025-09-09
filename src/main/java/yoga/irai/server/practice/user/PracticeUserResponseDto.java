package yoga.irai.server.practice.user;

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
public class PracticeUserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 2863029333768117233L;

    private UUID practiceUserId;
    private UUID userId;
    private String userName;
    private UUID practiceId;
    private String practiceTitle;
    private Integer attempt;
    private AppUtils.PracticeUserStatus status;
    private Long resumeTime;
    private Float rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
