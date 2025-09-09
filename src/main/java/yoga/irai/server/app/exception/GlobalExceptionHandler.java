package yoga.irai.server.app.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<AppResponseDto<Void>> handleAppExceptions(AppException e) {
        log.error(e.getMessage(), e);
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        return ResponseEntity.internalServerError().body(builder.errorMessage(e.getMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppResponseDto<Void>> handleOtherExceptions(Exception e) {
        log.error(e.getMessage(), e);
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        builder.errorMessage(AppUtils.Messages.UNEXPECTED_ERROR_1_MESSAGE.getMessage(e.getMessage()));
        return ResponseEntity.internalServerError().body(builder.build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppResponseDto<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        Map<String, String> errors = getErrorAttributes(e.getBindingResult());
        builder.errorMessage(AppUtils.Messages.VALIDATION_FAILED.getMessage()).errors(errors);
        return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AppResponseDto<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        builder.errorMessage(AppUtils.Messages.INVALID_REQUEST_BODY.getMessage());
        return ResponseEntity.badRequest().body(builder.build());
    }

    private static Map<String, String> getErrorAttributes(BindingResult bindingResult) {
        Map<String, String> errorAttributes = new HashMap<>();
        bindingResult.getAllErrors().forEach(error -> {
            String fieldName = error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
            }
            errorAttributes.put(fieldName, errorMessage);
        });
        return errorAttributes;
    }
}
