// File: src/test/java/yoga/irai/server/app/validator/FileMimeTypeValidatorTest.java
package yoga.irai.server.app.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import yoga.irai.server.app.AppProperties;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileMimeTypeValidatorTest {

    private FileMimeTypeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = mock(AppProperties.class);
        context = mock(ConstraintValidatorContext.class);
        when(appProperties.getMediaExtensions()).thenReturn("image/png,image/jpeg,application/pdf");
        validator = new FileMimeTypeValidator(appProperties);
        validator.initialize(null); // AllowedMimeTypes annotation not used in logic
    }

    @Test
    void testIsValid_NullFile_ReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValid_EmptyFile_ReturnsTrue() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void testIsValid_NullMimeType_ReturnsFalse() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);
        assertFalse(validator.isValid(file, context));
    }

    @Test
    void testIsValid_AllowedMimeType_ReturnsTrue() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void testIsValid_AllowedMimeType_UpperCase_ReturnsTrue() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("IMAGE/JPEG");
        assertTrue(validator.isValid(file, context));
    }

    @Test
    void testIsValid_NotAllowedMimeType_ReturnsFalse() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");
        assertFalse(validator.isValid(file, context));
    }
}