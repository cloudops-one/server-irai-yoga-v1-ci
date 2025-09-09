package yoga.irai.server.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import yoga.irai.server.app.exception.AppException;

class AppExceptionTest {

    @Test
    void testAppExceptionMessage() {
        String message = "Custom error message";
        AppException exception = new AppException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testAppExceptionIsThrowable() {
        assertThrows(AppException.class, () -> {
            throw new AppException("Test exception");
        });
    }
}
