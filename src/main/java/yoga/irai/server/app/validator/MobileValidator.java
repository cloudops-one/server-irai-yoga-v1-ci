package yoga.irai.server.app.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.authentication.dto.ForgotPasswordMobileRequestDto;
import yoga.irai.server.authentication.dto.SignInMobileRequestDto;
import yoga.irai.server.authentication.dto.SignUpMobileRequestDto;
import yoga.irai.server.authentication.dto.UserRequestDto;

public class MobileValidator implements ConstraintValidator<MobileValidate, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String userMobile;
        switch (value) {
            case SignUpMobileRequestDto signUpMobileRequestDto -> userMobile = signUpMobileRequestDto.getUserMobile();
            case UserRequestDto userRequestDto -> userMobile = userRequestDto.getUserMobile();
            case ContactDto contactDto -> userMobile = contactDto.getMobile();
            case SignInMobileRequestDto signInMobileRequestDto -> userMobile = signInMobileRequestDto.getUserMobile();
            case ForgotPasswordMobileRequestDto forgotPasswordMobileRequestDto ->
                userMobile = forgotPasswordMobileRequestDto.getUserMobile();
            default -> {
                return false;
            }
        }
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            PhoneNumber parsedNumber = phoneUtil.parse(userMobile, null);
            return phoneUtil.isValidNumber(parsedNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
