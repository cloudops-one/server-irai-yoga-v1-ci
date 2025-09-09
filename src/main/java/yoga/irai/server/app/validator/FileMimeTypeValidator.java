package yoga.irai.server.app.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import yoga.irai.server.app.AppProperties;

@Component
@RequiredArgsConstructor
public class FileMimeTypeValidator implements ConstraintValidator<AllowedMimeTypes, MultipartFile> {

    private final AppProperties appProperties;
    private Set<String> allowedMimeTypes;

    @Override
    public void initialize(AllowedMimeTypes constraintAnnotation) {
        // Load allowed MIME types from config
        allowedMimeTypes = Set.copyOf(Arrays.stream(appProperties.getMediaExtensions().split(",")).toList());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty())
            return true;
        String mimeType = file.getContentType();
        return mimeType != null && allowedMimeTypes.contains(mimeType.toLowerCase());
    }
}
