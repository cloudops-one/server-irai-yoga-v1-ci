package yoga.irai.server.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
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
public class SettingResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -2801277166858236106L;

    private String settingId;
    private String settingName;
    private transient List<Object> settingValue;
    private AppUtils.SettingStatus settingStatus;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
