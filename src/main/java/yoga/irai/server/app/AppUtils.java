package yoga.irai.server.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.context.SecurityContextHolder;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.setting.SettingEntity;
import yoga.irai.server.setting.SettingResponseDto;

public interface AppUtils {

    ModelMapper modelMapper = new ModelMapper();
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * map source object to target object.
     *
     * @param source      the source object
     * @param targetClass the target class type
     * @param <S>         the type of the source object
     * @param <T>         the type of the target object
     * @return the mapped target object
     */
    static <S, T> T map(S source, Class<T> targetClass) {
        try {
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            return modelMapper.map(source, targetClass);
        } catch (AppException e) {
            throw new AppException(AppUtils.Messages.CONVERT_ENTITY_TO_DTO_FAILED.getMessage());
        }
    }

    static <T extends HasId & HasPrimary> void updateIdsWithPrimary(List<T> items) {
        if (items != null) {
            AtomicInteger seq = new AtomicInteger(0);
            items.sort((a, b) -> Boolean.compare(b.getIsPrimary(), a.getIsPrimary()));
            for (var item : items) {
                item.setId(seq.getAndIncrement());
            }
        }
    }

    static <T extends HasId> void updateIds(List<T> items) {
        if (items != null) {
            AtomicInteger seq = new AtomicInteger(0);
            for (var item : items) {
                item.setId(seq.getAndIncrement());
            }
        }
    }

    interface HasPrimary {
        Boolean getIsPrimary();
    }

    interface HasId {
        void setId(Integer id);
    }


    /**
     * Map source object to an existing target object (updates target's fields).
     *
     * @param source the source object
     * @param target the target object (fields will be updated)
     * @param <S>    the type of the source object
     * @param <T>    the type of the target object
     */
    static <S, T> void map(S source, T target) {
        try {
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            modelMapper.map(source, target);
        } catch (Exception e) {
            throw new AppException(AppUtils.Messages.CONVERT_ENTITY_TO_DTO_FAILED.getMessage());
        }
    }

    /**
     * Maps a SettingEntity to a SettingResponseDto.
     *
     * @param settingEntity the SettingEntity to map
     * @return the mapped SettingResponseDto
     */
    static SettingResponseDto mapToResponse(SettingEntity settingEntity) {
        try {
            List<Object> valueList = objectMapper.readValue(
                    settingEntity.getSettingValue(), new TypeReference<>() {
                    }
            );
            SettingResponseDto responseDto = new SettingResponseDto();
            AppUtils.map(settingEntity, responseDto);
            responseDto.setSettingValue(valueList);
            return responseDto;
        } catch (JsonProcessingException e) {
            throw AppUtils.Messages.CONVERT_ENTITY_TO_DTO_FAILED.getException();
        }
    }

    static String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw AppUtils.Messages.CONVERT_ENTITY_TO_DTO_FAILED.getException();
        }
    }

    static <T> T readValue(String value, TypeReference<T> typeReference ) throws AppException {
        try {
            if(ObjectUtils.isEmpty(value)) {
                return null;
            }
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException e) {
            throw new AppException(AppUtils.Messages.CONVERT_ENTITY_TO_DTO_FAILED.getMessage());
        }
    }

    static Float calculateRating(List<Float> ratings, long ratingCount){
        if (!ratings.isEmpty()) {
            float sum = 0f;
            for (Float rating : ratings) {
                sum += rating;
            }
          return  sum / ratingCount;
        }
        return 0f;
    }

    static String format(String message, Object... args) {
        for (Object object : args) {
            message = message.replaceFirst(Pattern.quote("{}"), Matcher.quoteReplacement(object.toString()));
        }
        return message;
    }

    static UserEntity getPrincipalUser() {
        return ((UserPrincipalEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).user();
    }

    static UUID getPrincipalUserId() {
        return ((UserPrincipalEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).user().getUserId();
    }

    static UUID getPrincipalOrgId() {
        return ((UserPrincipalEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getOrgId();
    }

    static AppUtils.UserType getPrincipalUserType() {
        return ((UserPrincipalEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).user().getUserType();
    }

    static String getPrincipalName() {
        UserEntity user = ((UserPrincipalEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).user();
        return user.getUserFirstName() + ", " + user.getUserLastName();
    }

    static String decodeBase64ToString(String value) {
        return new String(decodeBase64ToByteArray(value), StandardCharsets.UTF_8);
    }

    static byte[] decodeBase64ToByteArray(String value) {
        return Base64.getDecoder().decode(value);
    }

    enum ModuleType {
        USERS, ORGANIZATION, PRACTICE, EVENT, SHORTS, POEM, PROGRAM, NEWS
    }

    enum UrlType {
        WEBSITE, FACEBOOK, TWITTER, INSTAGRAM, LINKEDIN, YOUTUBE, TELEGRAM, WHATSAPP, REGISTRATION_LINK, OTHER
    }

    enum EventStatus {
        ACTIVE, INACTIVE, COMPLETED, ONGOING, UPCOMING
    }

    enum OrganizationStatus {
        ACTIVE, INACTIVE
    }

    enum PracticeCategoryStatus {
        ACTIVE, INACTIVE
    }

    enum PracticeStatus {
        ACTIVE, INACTIVE
    }

    enum RefreshTokenStatus {
        ACTIVE, INACTIVE, REVOKED
    }

    enum UserStatus {
        NEW, ACTIVE, INACTIVE, DELETED, VERIFIED
    }

    enum PracticeUserStatus {
        STARTED, IN_PROGRESS, COMPLETE
    }

    enum ProgramUserStatus {
        STARTED, IN_PROGRESS, COMPLETE
    }

    enum LessonUserStatus {
        STARTED, IN_PROGRESS, COMPLETE
    }

    enum ShortsUserStatus {
        SEEN, NEW
    }

    enum UserType {
        PORTAL_USER, MOBILE_USER, KEYCLOAK_USER
    }

    enum PoemStatus {
        ACTIVE, INACTIVE
    }

    enum ShortsStatus {
        ACTIVE, INACTIVE
    }

    enum SettingStatus {
        ACTIVE, INACTIVE
    }

    enum ProgramStatus {
        ACTIVE, INACTIVE
    }

    enum NewsStatus {
        ACTIVE, INACTIVE
    }

    enum UserAoiStatus {
        ACTIVE, INACTIVE
    }

    enum NotificationStatus {
        IN_QUEUE, DONE
    }

    enum OptionType {
        SINGLE, MULTIPLE
    }

    enum NotificationTopic {
        PRACTICE, EVENT, SHORTS, POEM, PROGRAM, NEWS
    }

    @Getter
    enum SettingName {
        MODULE_TYPE("ModuleType"),
        URL_TYPE("UrlType"),
        EVENT_STATUS("EventStatus"),
        REFRESH_TOKEN_STATUS("RefreshTokenStatus"),
        USER_STATUS("UserStatus"),
        PRACTICE_USER_STATUS("PracticeUserStatus"),
        USER_TYPE("UserType"),
        POEM_STATUS("PoemStatus"),
        ORGANIZATION_STATUS("OrganizationStatus"),
        PRACTICE_CATEGORY_STATUS("PracticeCategoryStatus"),
        PRACTICE_STATUS("PracticeStatus"),
        SHORTS_STATUS("ShortsStatus"),
        PROGRAM_STATUS("ProgramStatus"),
        COUNTRY("Country"),
        USER_AOI_QUESTIONS("UserAoiQuestions"),
        SHORTS_TAGS("ShortsTags"),
        PRACTICE_TAGS("PracticeTags"),
        POEM_TAGS("PoemTags"),
        PROGRAM_TAGS("ProgramTags"),
        PROGRAM_FLAG("ProgramFlag"),
        PROGRAM_USER_STATUS("ProgramUserStatus"),
        LESSON_USER_STATUS("LessonUserStatus"),
        NEWS_STATUS("NewsStatus"),
        NEWS_TAGS("NewsTags"),
        ;

        private final String setting;

        SettingName(String setting) {
            this.setting = setting;
        }

    }

    enum ProgramFlag {
        MOST_WATCHED("Most Watched"), TRENDING("Trending");

        private final String flag;

        ProgramFlag(String flag) {
            this.flag = flag;
        }

        public String getValue() {
            return flag;
        }
    }

    enum Gender {
        MALE("Male"), FEMALE("Female"), OTHERS("Others"), PREFER_NOT_TO_SAY("Prefer Not to Say");

        private final String genderStr;

        Gender(String genderStr) {
            this.genderStr = genderStr;
        }

        @JsonCreator
        public static Gender fromValue(String value) {
            for (Gender gd : values()) {
                if (gd.genderStr.equals(value) || gd.name().equals(value)) {
                    return gd;
                }
            }
            throw new IllegalArgumentException("Unknown blood group: " + value);
        }
    }

    enum BloodGroup {
        A_POSITIVE("A+"), A_NEGATIVE("A-"), B_POSITIVE("B+"), B_NEGATIVE("B-"), AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"), O_POSITIVE("O+"), O_NEGATIVE("O-");

        private final String bloodGroupStr;

        BloodGroup(String bloodGroup) {
            this.bloodGroupStr = bloodGroup;
        }

        @JsonValue
        public String getValue() {
            return bloodGroupStr;
        }

        @JsonCreator
        public static BloodGroup fromValue(String value) {
            for (BloodGroup bg : values()) {
                if (bg.bloodGroupStr.equals(value) || bg.name().equals(value)) {
                    return bg;
                }
            }
            throw new IllegalArgumentException("Unknown blood group: " + value);
        }
    }

    enum Messages {
        INVALID_REQUEST_BODY("Invalid request body"),
        INVALID_PASSWORD("Invalid password"),
        VALIDATION_FAILED("Validation failed"),
        ACCESS_DENIED("Access denied"),
        UNEXPECTED_ERROR_1_MESSAGE("Unexpected error occurred: {}"),
        ERROR_LOADING_PRIVATE_KEY("Error loading private key"),
        ERROR_LOADING_PUBLIC_KEY("Error loading public key"),
        ERROR_GENERATING_ACCESS_TOKEN("Error generating access token"),
        SIGN_IN_SUCCESS("Signed in successful"),
        SIGN_OUT_SUCCESS("Signed out successful"),
        SIGN_OUT_FAILED("Signed out failed"),
        ADD_SUCCESS("Added successfully"),
        UPDATE_SUCCESS("Updated successfully"),
        DELETE_SUCCESS("Deleted successfully"),
        SEARCH_FOUND("Search found"),
        USER_FOUND("User found"),
        USER_NOT_FOUND("User not found"),
        USER_NOT_FOUND_1_USER_ID("User not found, userId:{}"),
        EVENT_FOUND("Event found"),
        EVENT_NOT_FOUND("Event not found"),
        ORGANIZATION_NOT_FOUND("Organization not found"),
        ORGANIZATION_FOUND("Organization found"),
        PRACTICE_CATEGORY_FOUND("Practice Category found"),
        PRACTICE_CATEGORY_NOT_FOUND("Practice Category not found"),
        PRACTICE_FOUND("Practice found"),
        PRACTICE_USER_NOT_FOUND("Practice user not found"),
        POEM_FOUND("Poem found"),
        POEM_NOT_FOUND("Poem not found"),
        SETTING_NOT_FOUND("Setting not found"),
        SHORTS_FOUND("shorts found"),
        SHORTS_NOT_FOUND("shorts not found"),
        NAME_EXISTS("Name already exists"),

        STORAGE_FILE_UPLOADED_SUCCESS_1_STORAGE_ID("Storage uploaded successfully, storageId:{}"),
        STORAGE_FILE_DELETED_SUCCESS_1_NAME("Storage deleted successfully, storageId:{}"),
        STORAGE_NOT_FOUND("Storages not found"),
        STORAGE_SEARCHES_FOUND("Storage search found"),
        STORAGE_APP_DEFAULT_FILES_CANNOT_BE_DELETED_1_STORAGE_NAME("App default storage files cannot be deleted, storageName:{}"),

        REFRESH_TOKEN_IS_INVALID("Refresh token not found"),
        REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
        ACCESS_TOKEN_GENERATED("Access token generated successfully"),

        EMAIL_ALREADY_EXISTS("Email already exists"),
        VERIFICATION_SUCCESS("Email verification successful"),
        MOBILE_ALREADY_EXISTS("Mobile number already exists"),

        MOBILE_VERIFICATION_SUCCESS_1_USER_ID("Mobile number verification successful, userId:{}"),
        PASSWORD_SET_SUCCESS("Password set successfully"),

        INVALID_OTP("Invalid OTP"),
        OTP_SEND_SUCCESS("OTP sent successfully"),
        OTP_RESEND_SUCCESS("OTP resend successful"),
        OTP_EXPIRED("OTP expired"),

        CONVERT_ENTITY_TO_DTO_FAILED("Failed to convert audit to DTO"),

        PAYLOAD_VALIDATION_FAILED_FIELD_NOTFOUND_1_FIELD("Field not found, field:{}"),
        PASSWORD_RESET_SUCCESS("Password reset successful"),
        PROGRAM_NOT_FOUND("Program not found"),
        SECTION_NOT_FOUND("Section not found"),
        LESSON_NOT_FOUND("Lesson not found"),
        SECTION_FOUND("Section found"),
        VERIFIED_USER_RESET_PASSWORD("Verified User, Please reset password."),
        PLEASE_RESET_PASSWORD("Please reset your password!"),
        USER_ACCOUNT_DEACTIVATED("Account deactivated, Please contact administrator."),
        USER_STATS_FOUND("User Stats found"),
        NEWS_FOUND("News found"),
        NEWS_NOT_FOUND("News not found"),
        CLASS_NOT_ENUM("{} is not an enum class Skipping..."),
        SETTING_ENUM_NOT_FOUND("Setting enum not found :{}"),
        SETTING_VALIDATED("Setting validated successfully:{}"),
        SETTING_MISMATCH("Setting mismatch:{} ,EnumKeys: {}, DBEnumKeys:{}"),
        STORAGE_SYNCED_SUCCESS("Storage synchronized successfully"),
        PROGRAM_USER_NOT_FOUND("Program user not found"),
        LESSON_USER_NOT_FOUND("Lesson user not found");


        public static final String FCM_TOKEN_ADDED_SUCCESS = "FCM token added successfully";
        private final String message;

        Messages(String message) {
            this.message = message;
        }

        public String getMessage(Object... args) {
            return format(message, args);
        }

        public AppException getException(Object... args) {
            return new AppException(format(message, args));
        }

        public void throwException(Object... args) {
            throw new AppException(format(message, args));
        }
    }

    class Constants {

        public static final String RSA = "RSA";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER = "Bearer ";

        public static final String DEFAULT_EMAIL_ID = "sample@test.in";
        public static final String DEFAULT_MOBILE_NUMBER = "+919999999999";
        public static final String SETTING_PACKAGE = "yoga.irai.server.app.AppUtils$";
        public static final String DEFAULT_NOTIFICATION_TOPIC = "irai-yoga_";

        public static final String REFRESH_TOKEN_BLANK = "Refresh Token is blank";
        public static final String EMAIL_BLANK = "Email is blank";
        public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
        public static final String INVALID_MOBILE_NUMBER_FORMAT = "Invalid mobile number format";

        public static final String STORAGE_SYNCED_AT = "STORAGE_SYNC";

        public static final String OTP_REQUIRED = "OTP is required";

        public static final String INVALID_PASSWORD_FORMAT = "Invalid password format. Password must contain " +
                "at least 8 characters, including uppercase, lowercase, numbers, and special characters.";

        public static final String USER_FIRST_NAME_BLANK = "First name is blank";
        public static final String USER_LAST_NAME_BLANK = "Last name is blank";
        public static final String USER_ID_BLANK = "User Id is blank";

        public static final String PASSWORD_BLANK = "Password is blank";

        public static final String VALIDATION_FAILED_ID_BLANK = "Id is blank";
        public static final String VALIDATION_FAILED_TYPE_BLANK = "Type is blank";
        public static final String VALIDATION_FAILED_NAME_BLANK = "Name is blank";
        public static final String VALIDATION_FAILED_DESCRIPTION_BLANK = "Description is blank";
        public static final String VALIDATION_FAILED_DATE_TIME_BLANK = "Date and Time is blank";
        public static final String VALIDATION_FAILED_TIME_BLANK = "Time is blank";
        public static final String VALIDATION_FAILED_DATE_BLANK = "Date is blank";
        public static final String VALIDATION_FAILED_GENDER_BLANK = "Gender is blank";
        public static final String VALIDATION_FAILED_BLOOD_GROUP_BLANK = "Blood Group is Blank";
        public static final String VALIDATION_FAILED_ADDRESSES_BLANK = "Addresses are blank";
        public static final String VALIDATION_FAILED_CONTACTS_BLANK = "Contacts are blank";
        public static final String VALIDATION_FAILED_URLS_BLANK = "URLs are blank";
        public static final String VALIDATION_FAILED_FILE_BLANK = "File is blank";
        public static final String VALIDATION_FAILED_EMAIL_BLANK = "Email is blank";
        public static final String VALIDATION_FAILED_EMAIL_FORMAT_INVALID = "Email format is invalid";
        public static final String VALIDATION_FAILED_MOBILE_BLANK = "Mobile number is blank";
        public static final String VALIDATION_FAILED_ADDRESS_LINE_1_BLANK = "Address Line 1 is blank";
        public static final String VALIDATION_FAILED_ADDRESS_LINE_2_BLANK = "Address Line 2 is blank";
        public static final String VALIDATION_FAILED_CITY_BLANK = "City is blank";
        public static final String VALIDATION_FAILED_STATE_PROVINCE_BLANK = "State or Province is blank";
        public static final String VALIDATION_FAILED_POSTAL_CODE_BLANK = "Postal code is blank";
        public static final String VALIDATION_FAILED_COUNTRY_BLANK = "Country is blank";
        public static final String VALIDATION_FAILED_REGISTRATION_BLANK = "Registration Number is blank";
        public static final String VALIDATION_FAILED_RECOMMENDED_BLANK = "Recommended is blank";
        public static final String VALIDATION_FAILED_TEXT_BLANK = "Text is blank";
        public static final String VALIDATION_FAILED_LIST_BLANK = "List is blank";
        public static final String VALIDATION_RATING_MIN_0_MAX_5 = "Rating should be between 0 and 5";

        public static final String DEVICE_CODE_BLANK = "Device Code is blank";
        public static final String DEVICE_NAME_BLANK = "Device Name is blank";
        public static final String DEVICE_TYPE_BLANK = "Device Type is blank";

        public static final String EMAIL_FROM_ADDRESS = "support@terv.pro";

        public static final String OTP_MESSAGE_1_OTP_2_EXPIRY_TIME = "Your OTP is %s. It is valid for %d minutes. Do not share it with anyone \n- Irai yoga";
        public static final String OTP_EMAIL_SUBJECT = "Irai Yoga OTP Verification";
        public static final String OTP_EMAIL_BODY = """
                Hello %s,
                
                Your One-Time Password (OTP) for verification is: %s
                
                This code is valid for %d minutes. Do not share it with anyone.
                If you did not request this code, please ignore this message.
                
                Thank you,
                The Irai Yoga Team
                """;

        public static final String SECURITY_SCHEME_NAME = "bearerAuth";
        public static final String STORAGE_TABLE_ORPHANED = "STORAGE_TABLE_ORPHANED";
        public static final String STORAGE_BUCKET_ORPHANED = "STORAGE_BUCKET_ORPHANED";
        public static final String VALIDATION_FAILED_MESSAGE_BLANK = "Message is blank";
        public static final String REPLY_MAIL_SUB = "Thank You for Contacting Irai Yoga";
        public static final String REPLY_MAIL_BODY = """
                Hi %s,
                
                Thank you for reaching out to us!
                
                We’ve received your message and our team will review it shortly. One of our representatives will get back to you within 24 hours. In the meantime, if you have any additional details to share, feel free to reply directly to this email.
                
                We appreciate your interest in Irai yoga, and we’ll do our best to assist you as quickly as possible.
                
                Warm regards,
                The Irai Yoga Team
                """;
        public static final String ENQUIRY_MAIL_TO_SUPPORT_SUB = "Irai";
        public static final String ENQUIRY_MAIL_TO_SUPPORT_BODY = """
                Hi,
                Hi Support Team,
                
                You’ve received a new inquiry through the Contact Us form.
                
                Here are the details:
                Name: %s
                Email: %s
                Message:
                %s
                
                Please follow up with the user as soon as possible.
                
                Regards,
                Irai Yoga Team.
                """;
        public static final String VALIDATION_FAILED_CODE_BLANK = "Device Code is blank";
        public static final String VALIDATION_FAILED_TOKEN_BLANK = "Fcm token is blank";

        private Constants() {
        }
    }
}
