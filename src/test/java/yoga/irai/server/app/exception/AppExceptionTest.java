package yoga.irai.server.app.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    void testMessageConstructor() {
        AppException ex = new AppException("Test message");
        assertEquals("Test message", ex.getMessage());
    }
}