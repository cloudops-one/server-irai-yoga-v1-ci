package yoga.irai.server.app.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleAppException() {
        AppException ex = new AppException("App error");
        ResponseEntity<AppResponseDto<Void>> response = handler.handleAppExceptions(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<AppResponseDto<Void>> response = handler.handleOtherExceptions(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "must not be null");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(exception.getMessage()).thenReturn("Validation failed");

        ResponseEntity<AppResponseDto<Void>> response = handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AppResponseDto<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(AppUtils.Messages.VALIDATION_FAILED.getMessage(), body.getErrorMessage());
        Map<String, String> errors = body.getErrors();
        assertNotNull(errors);
        assertEquals("must not be null", errors.get("fieldName"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Malformed JSON request");

        ResponseEntity<AppResponseDto<Void>> response = handler.handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AppResponseDto<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(AppUtils.Messages.INVALID_REQUEST_BODY.getMessage(), body.getErrorMessage());
    }
}