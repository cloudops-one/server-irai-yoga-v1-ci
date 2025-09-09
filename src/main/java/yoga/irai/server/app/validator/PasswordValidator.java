package yoga.irai.server.app.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import yoga.irai.server.app.AppUtils;

public class PasswordValidator implements ConstraintValidator<PasswordValidate, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            if (ObjectUtils.isEmpty(value)) {
                return false;
            }
            String password = (String) value;
            String decodedPassword = AppUtils.decodeBase64ToString(password);
            String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,64}$";
            Matcher matcher = Pattern.compile(pattern).matcher(decodedPassword);
            return matcher.find();
        } catch (Exception e) {
            return false;
        }
    }
}
