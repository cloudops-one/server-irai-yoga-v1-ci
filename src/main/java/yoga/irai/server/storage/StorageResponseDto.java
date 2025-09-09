package yoga.irai.server.storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StorageResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3256246477342226567L;

    private UUID storageId;
    private String storageName;
    private String storageUrl;
    private String contentType;
    private String extension;
    private Long size;
    private String tags;
    private UUID createdBy;
    private String createdByName;
    private ZonedDateTime createdAt;
}
