package yoga.irai.server.app.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import yoga.irai.server.app.AppUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void testIsValid_EmptyValue_ReturnsFalse() {
        assertFalse(validator.isValid(null, context));
        assertFalse(validator.isValid("", context));
    }

    @Test
    void testIsValid_ExceptionThrown_ReturnsFalse() {
        Object notAString = new Object();
        assertFalse(validator.isValid(notAString, context));
    }

    @Test
    void testIsValid_DecodedPasswordMatchesPattern_ReturnsTrue() {
        String encoded = "encoded";
        String decoded = "Abcdefg!1";
        try (MockedStatic<AppUtils> utils = mockStatic(AppUtils.class)) {
            utils.when(() -> AppUtils.decodeBase64ToString(encoded)).thenReturn(decoded);
            assertTrue(validator.isValid(encoded, context));
        }
    }

    @Test
    void testIsValid_DecodedPasswordDoesNotMatchPattern_ReturnsFalse() {
        String encoded = "encoded";
        String decoded = "abcdefg1"; // No uppercase, no special char
        try (MockedStatic<AppUtils> utils = mockStatic(AppUtils.class)) {
            utils.when(() -> AppUtils.decodeBase64ToString(encoded)).thenReturn(decoded);
            assertFalse(validator.isValid(encoded, context));
        }
    }

    @Test
    void testIsValid_DecodeThrowsException_ReturnsFalse() {
        String encoded = "encoded";
        try (MockedStatic<AppUtils> utils = mockStatic(AppUtils.class)) {
            utils.when(() -> AppUtils.decodeBase64ToString(encoded)).thenThrow(new RuntimeException("fail"));
            assertFalse(validator.isValid(encoded, context));
        }
    }
}