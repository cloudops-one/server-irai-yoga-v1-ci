package yoga.irai.server.app.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileMimeTypeValidator.class)
@Documented
public @interface AllowedMimeTypes {
    String message() default "Unsupported file type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
