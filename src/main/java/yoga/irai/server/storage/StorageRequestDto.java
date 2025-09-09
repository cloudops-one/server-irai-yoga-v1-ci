package yoga.irai.server.storage;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.validator.AllowedMimeTypes;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class StorageRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -3256246477345246567L;

    @AllowedMimeTypes
    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_FILE_BLANK)
    private transient MultipartFile file;

    @NotNull(message = AppUtils.Constants.VALIDATION_FAILED_TYPE_BLANK)
    private AppUtils.ModuleType moduleType;
}
