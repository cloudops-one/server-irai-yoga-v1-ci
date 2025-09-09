package yoga.irai.server.app.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import yoga.irai.server.app.AppUtils;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface PasswordValidate {

    String message() default AppUtils.Constants.INVALID_PASSWORD_FORMAT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
