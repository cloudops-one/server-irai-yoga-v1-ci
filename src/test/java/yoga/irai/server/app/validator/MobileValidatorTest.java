package yoga.irai.server.app.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.authentication.dto.ForgotPasswordMobileRequestDto;
import yoga.irai.server.authentication.dto.SignInMobileRequestDto;
import yoga.irai.server.authentication.dto.SignUpMobileRequestDto;
import yoga.irai.server.authentication.dto.UserRequestDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MobileValidatorTest {

    private final MobileValidator validator = new MobileValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void testIsValid_NullValue_ReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValid_UnsupportedType_ReturnsFalse() {
        assertFalse(validator.isValid("unsupported", context));
    }

    @Test
    void testIsValid_SignUpMobileRequestDto_ValidNumber_ReturnsTrue() throws Exception {
        SignUpMobileRequestDto dto = mock(SignUpMobileRequestDto.class);
        when(dto.getUserMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(true);

            assertTrue(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_SignUpMobileRequestDto_InvalidNumber_ReturnsFalse() throws Exception {
        SignUpMobileRequestDto dto = mock(SignUpMobileRequestDto.class);
        when(dto.getUserMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(false);

            assertFalse(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_SignUpMobileRequestDto_NumberParseException_ReturnsFalse() throws Exception {
        SignUpMobileRequestDto dto = mock(SignUpMobileRequestDto.class);
        when(dto.getUserMobile()).thenReturn("invalid");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("invalid", null)).thenThrow(new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "error"));

            assertFalse(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_UserRequestDto_ValidNumber_ReturnsTrue() throws Exception {
        UserRequestDto dto = mock(UserRequestDto.class);
        when(dto.getUserMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(true);

            assertTrue(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_ContactDto_ValidNumber_ReturnsTrue() throws Exception {
        ContactDto dto = mock(ContactDto.class);
        when(dto.getMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(true);

            assertTrue(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_SignInMobileRequestDto_ValidNumber_ReturnsTrue() throws Exception {
        SignInMobileRequestDto dto = mock(SignInMobileRequestDto.class);
        when(dto.getUserMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(true);

            assertTrue(validator.isValid(dto, context));
        }
    }

    @Test
    void testIsValid_ForgotPasswordMobileRequestDto_ValidNumber_ReturnsTrue() throws Exception {
        ForgotPasswordMobileRequestDto dto = mock(ForgotPasswordMobileRequestDto.class);
        when(dto.getUserMobile()).thenReturn("1234567890");

        try (MockedStatic<PhoneNumberUtil> phoneUtilStatic = mockStatic(PhoneNumberUtil.class)) {
            PhoneNumberUtil phoneUtil = mock(PhoneNumberUtil.class);
            PhoneNumber phoneNumber = mock(PhoneNumber.class);
            phoneUtilStatic.when(PhoneNumberUtil::getInstance).thenReturn(phoneUtil);
            when(phoneUtil.parse("1234567890", null)).thenReturn(phoneNumber);
            when(phoneUtil.isValidNumber(phoneNumber)).thenReturn(true);

            assertTrue(validator.isValid(dto, context));
        }
    }
}