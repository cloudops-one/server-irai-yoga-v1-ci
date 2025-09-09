package yoga.irai.server.authentication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.audit.Auditable;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.DeviceEntity;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.repository.DeviceRepository;
import yoga.irai.server.authentication.repository.UserRepository;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.provider.OtpService;
import yoga.irai.server.setting.SettingService;
import yoga.irai.server.storage.StorageService;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final OtpService otpService;
    private final AppProperties appProperties;
    private final StorageService storageService;
    private final SettingService settingService;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final OrganizationService organizationService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Loads user details by username (userId).
     *
     * @param userId
     *            the user ID
     * @return UserDetails object containing user information
     */
    @Override
    public UserDetails loadUserByUsername(String userId) {
        return new UserPrincipalEntity(getUserById(UUID.fromString(userId)));
    }

    // CRUD OPERATION

    /**
     * Adds a new user to the system.
     *
     * @param userCreationRequestDto
     *            the user creation request data transfer object
     * @return the created UserEntity
     */
    public UserEntity addUser(@Valid UserCreationRequestDto userCreationRequestDto) {
        String password = AppUtils.decodeBase64ToString(userCreationRequestDto.getPassword());
        UserEntity userEntity = AppUtils.map(userCreationRequestDto, UserEntity.class);
        userEntity.setUserStatus(AppUtils.UserStatus.INACTIVE);
        userEntity.setUserType(AppUtils.UserType.PORTAL_USER);
        userEntity.setPasswordHash(bCryptPasswordEncoder.encode(password));
        AppUtils.updateIdsWithPrimary(userEntity.getAddresses());
        userEntity = userRepository.save(userEntity);
        return userEntity;
    }

    /**
     * Updates an existing user in the system.
     *
     * @param userId
     *            the ID of the user to update
     * @param userRequestDto
     *            the user request data transfer object containing updated
     *            information
     * @return the updated UserEntity
     */
    @Transactional
    public UserEntity updateUser(UUID userId, @Valid UserRequestDto userRequestDto) {
        UserEntity userEntity = getUserById(userId);
        if (!userRequestDto.getUserIconStorageId().equals(userEntity.getUserIconStorageId())) {
            storageService.deleteStorageById(userEntity.getUserIconStorageId());
        }
        AppUtils.map(userRequestDto, userEntity);
        userEntity.setUserId(userId);
        userEntity.setOrgId(userRequestDto.getOrgId());
        AppUtils.updateIdsWithPrimary(userEntity.getAddresses());
        return userRepository.save(userEntity);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId
     *            the ID of the user to delete
     */
    public UserEntity getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppUtils.Messages.USER_NOT_FOUND_1_USER_ID.getException(userId));
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of users per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ascending or descending)
     * @param keyword
     *            an optional search keyword to filter users by name or email
     * @return a Page object containing the paginated list of UserEntity objects
     */
    public Page<UserEntity> getUsers(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        return switch (AppUtils.getPrincipalUserType()) {
            case PORTAL_USER -> userRepository.search(keyword, AppUtils.getPrincipalOrgId(), pageable);
            case KEYCLOAK_USER -> userRepository.search(keyword, null, pageable);
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    // MOBILE SIGN UP

    /**
     * Creates a new user with default values for mobile sign-up.
     *
     * @param signUpRequestDto
     *            the sign-up request data transfer object containing user
     *            information
     * @return a SignUpResponseDto containing the created user's ID
     */
    public SignUpResponseDto createUser(@Valid SignUpRequestDto signUpRequestDto) {
        UserEntity userEntity = UserEntity.builder().userFirstName(signUpRequestDto.getFirstName())
                .userLastName(signUpRequestDto.getLastName())
                .userEmail(signUpRequestDto.getFirstName() + signUpRequestDto.getLastName()
                        + AppUtils.Constants.DEFAULT_EMAIL_ID)
                .userMobile(AppUtils.Constants.DEFAULT_MOBILE_NUMBER).userType(AppUtils.UserType.MOBILE_USER)
                .orgId(UUID.fromString(appProperties.getDefaultOrganizationId()))
                .createdBy(UUID.fromString(appProperties.getDefaultUserId()))
                .updatedBy(UUID.fromString(appProperties.getDefaultUserId())).userStatus(AppUtils.UserStatus.NEW)
                .gender(AppUtils.Gender.PREFER_NOT_TO_SAY).bloodGroup(AppUtils.BloodGroup.B_POSITIVE).skipAudit(true)
                .build();
        userEntity = userRepository.save(userEntity);
        return SignUpResponseDto.builder().userId(userEntity.getUserId()).build();
    }

    /**
     * Retrieves a user by their mobile number
     *
     * @param signUpMobileRequestDto
     *            the user's mobile number
     */
    @Transactional
    public void addMobile(@Valid SignUpMobileRequestDto signUpMobileRequestDto) {
        String userMobile = signUpMobileRequestDto.getUserMobile();
        UUID userId = signUpMobileRequestDto.getUserId();
        UserEntity userEntity = getUserById(userId);
        if (userRepository.existsByUserMobileAndUserStatus(userMobile, AppUtils.UserStatus.ACTIVE)) {
            throw AppUtils.Messages.MOBILE_ALREADY_EXISTS.getException(userMobile);
        }
        userEntity.setUserMobile(userMobile);
        userEntity.setUserType(AppUtils.UserType.MOBILE_USER);
        userEntity.setSkipAudit(true);
        otpService.sendMobileOtp(signUpMobileRequestDto);
        userRepository.save(userEntity);
    }

    /**
     * Updates the mobile verification status for a user.
     *
     * @param userId
     *            the ID of the user to update
     */
    public void updateMobileVerification(UUID userId) {
        UserEntity userEntity = getUserById(userId);
        userEntity.setMobileVerified(true);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
    }

    /**
     * Adds an email address to a user.
     *
     * @param signUpEmailRequestDto
     *            the sign-up email request data transfer object containing user ID
     *            and email
     */
    @Transactional
    public void addEmail(@Valid SignUpEmailRequestDto signUpEmailRequestDto) {
        UUID userId = signUpEmailRequestDto.getUserId();
        String email = signUpEmailRequestDto.getEmail();
        UserEntity userEntity = getUserById(userId);
        if (userRepository.existsByUserEmailAndUserStatus(email, AppUtils.UserStatus.ACTIVE)) {
            throw AppUtils.Messages.EMAIL_ALREADY_EXISTS.getException(userId);
        }
        userEntity.setUserEmail(email);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
        otpService.sendEmailOtp(signUpEmailRequestDto);
    }

    /**
     * Updates the email verification status for a user.
     *
     * @param userId
     *            the ID of the user to update
     */
    public void updateEmailVerification(UUID userId) {
        UserEntity userEntity = getUserById(userId);
        userEntity.setEmailVerified(true);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
    }

    /**
     * Saves the password for a user.
     *
     * @param signUpPasswordRequestDto
     *            the sign-up password request data transfer object containing user
     *            ID and password
     */
    public void savePassword(@Valid SignUpPasswordRequestDto signUpPasswordRequestDto) {
        UserEntity userEntity = getUserById(signUpPasswordRequestDto.getUserId());
        String password = AppUtils.decodeBase64ToString(signUpPasswordRequestDto.getPassword());
        userEntity.setPasswordHash(bCryptPasswordEncoder.encode(password));
        userEntity.setUserStatus(AppUtils.UserStatus.ACTIVE);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
    }

    // INTERNAL SERVICE

    /**
     * Retrieves the user's name by their ID.
     *
     * @param userId
     *            the ID of the user
     * @return the user's name
     */
    public String getUserNameById(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) {
            return "";
        }
        return userEntity.getUserFirstName() + " " + userEntity.getUserLastName();
    }

    /**
     * Retrieves a map of user IDs to their names for a list of user IDs.
     *
     * @param list
     *            the list of user IDs
     * @return a map where the key is the user ID and the value is the user's name
     */
    public Map<UUID, String> getUserNamesByIds(List<UUID> list) {
        List<UserEntity> usersEntities = userRepository.findAllById(list);
        return usersEntities.stream().collect(Collectors.toMap(UserEntity::getUserId,
                user -> user.getUserFirstName() + " " + user.getUserLastName()));
    }

    // ACTIVITY SERVICE

    /**
     * Updates the last login timestamp for a user.
     *
     * @param userId
     *            the ID of the user to update
     */
    public void updateLastLogin(UUID userId) {
        UserEntity userEntity = getUserById(userId);
        userEntity.setLastLoginAt(ZonedDateTime.now());
        userRepository.save(userEntity);
    }

    /**
     * Checks if a user exists by email or mobile number.
     *
     * @param userEmail
     *            - User's email
     * @return the user ID if found
     */
    public UserEntity checkCredentials(String userEmail) {
        UserEntity userEntity = userRepository.findByUserEmail(userEmail);
        if (ObjectUtils.isEmpty(userEntity)) {
            throw AppUtils.Messages.USER_NOT_FOUND.getException();
        }
        return userEntity;
    }

    /**
     * Checks if a user exists by email or mobile number.
     *
     * @param userMobile
     *            - user's mobile number
     *
     * @return the user ID if found
     */
    public UserEntity checkCredentialsByUserMobile(String userMobile) {
        UserEntity userEntity = userRepository.findByUserMobile(userMobile);
        if (ObjectUtils.isEmpty(userEntity)) {
            throw AppUtils.Messages.USER_NOT_FOUND.getException();
        }
        return userEntity;
    }

    /**
     * Updates the user status.
     *
     * @param userId
     *            the ID of the user to update
     * @param status
     *            the new status to set for the user
     */
    public void updateUserStatus(UUID userId, AppUtils.UserStatus status) {
        UserEntity userEntity = getUserById(userId);
        userEntity.setUserStatus(status);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
    }

    /**
     * Finds or creates a device for a user.
     *
     * @param user
     *            the user audit
     * @param deviceCode
     *            the unique device code
     * @param deviceType
     *            the type of the device (e.g., mobile, tablet, etc.)
     * @param deviceName
     *            the name of the device
     * @return the DeviceEntity representing the found or created device
     */
    public DeviceEntity findOrCreateDevice(UserEntity user, String deviceCode, String deviceType, String deviceName) {
        return deviceRepository.findByDeviceCodeAndUser(deviceCode, user).orElseGet(() -> {
            DeviceEntity newDeviceEntity = DeviceEntity.builder().user(user).deviceCode(deviceCode)
                    .deviceName(deviceName).deviceType(deviceType).createdAt(ZonedDateTime.now()).build();
            return deviceRepository.save(newDeviceEntity);
        });
    }

    /**
     * Updates the password for a user during the sign-up process.
     *
     * @param signUpPasswordRequestDto
     *            - the sign-up password request data transfer object
     */
    public void updatePassword(SignUpPasswordRequestDto signUpPasswordRequestDto) {
        UserEntity userEntity = userRepository.getUserEntityByUserIdAndUserStatus(signUpPasswordRequestDto.getUserId(),
                AppUtils.UserStatus.VERIFIED);
        if (ObjectUtils.isEmpty(userEntity)) {
            throw new AppException(AppUtils.Messages.PLEASE_RESET_PASSWORD.getMessage());
        }
        if (!signUpPasswordRequestDto.getUserId().equals(UUID.fromString(appProperties.getE2eUserId()))) {
            userEntity.setPasswordHash(bCryptPasswordEncoder
                    .encode(AppUtils.decodeBase64ToString(signUpPasswordRequestDto.getPassword())));
        }
        userEntity.setUserStatus(AppUtils.UserStatus.ACTIVE);
        userEntity.setSkipAudit(true);
        userRepository.save(userEntity);
    }

    /**
     * Resets the user's password.
     *
     * @param userId
     *            the ID of the user whose password is to be reset
     * @param updatePasswordDto
     *            the data transfer object containing the old and new passwords
     */
    public void resetUserPassword(UUID userId, UpdatePasswordDto updatePasswordDto) {
        UserEntity userEntity = getUserById(userId);
        if (!bCryptPasswordEncoder.matches(AppUtils.decodeBase64ToString(updatePasswordDto.getOldPassword()),
                userEntity.getPasswordHash())) {
            throw AppUtils.Messages.INVALID_PASSWORD.getException();
        }
        userEntity.setPasswordHash(
                bCryptPasswordEncoder.encode(AppUtils.decodeBase64ToString(updatePasswordDto.getNewPassword())));
        userRepository.save(userEntity);
    }

    /**
     * Retrieves user statistics based on the user's type.
     *
     * @return UserStatsDto containing user statistics
     */
    public UserStatsDto getUserStats() {
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());
        orgIds.add(UUID.fromString(appProperties.getDefaultOrganizationId()));
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER ->
                userRepository.getUserStats(null, AppUtils.UserStatus.ACTIVE, AppUtils.UserType.MOBILE_USER);
            case PORTAL_USER ->
                userRepository.getUserStats(orgIds, AppUtils.UserStatus.ACTIVE, AppUtils.UserType.MOBILE_USER);
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Updates the user's Area of Interest (AOI) answers.
     *
     * @param answers
     *            the user's AOI answers in a specific format
     */
    public void updateUserAoiAnswers(String answers) {
        UserEntity userEntity = getUserById(AppUtils.getPrincipalUserId());
        userEntity.setUserAoi(answers);
        userRepository.save(userEntity);
    }

    /**
     * Retrieves the user's Area of Interest (AOI) questions and options.
     *
     * @param userEntity
     *            the user audit for which to retrieve AOI questions
     * @return a list of UserAoiDto containing AOI questions and options
     */
    public List<UserAoiDto> getUserAoi(UserEntity userEntity) {
        List<UserAoiDto> userAoiDtos = AppUtils.readValue(
                settingService.getSettingBySettingName("UserAoiQuestions").getSettingValue(), new TypeReference<>() {
                });
        String answers = getUserById(userEntity.getUserId()).getUserAoi();
        Map<Integer, List<Integer>> answerMap = getIntegerListMap(answers);
        if (ObjectUtils.isNotEmpty(userAoiDtos)) {
            for (UserAoiDto question : userAoiDtos) {
                int qId = question.getQuestionId();
                for (UserAoiOptionDto option : question.getOptions()) {
                    option.setSelected(answerMap.containsKey(qId) && answerMap.get(qId).contains(option.getId()));
                }
            }
        }
        return userAoiDtos;
    }

    /**
     * Parses the user's AOI answers from a string format into a map.
     *
     * @param answers
     *            the user's AOI answers in a specific format
     * @return a map where the key is the question ID and the value is a list of
     *         selected option IDs
     */
    private static Map<Integer, List<Integer>> getIntegerListMap(String answers) {
        Map<Integer, List<Integer>> answerMap = new HashMap<>();
        for (String q : answers.split(";")) {
            String[] parts = q.split(":");
            if (parts.length == 2) {
                int qId = Integer.parseInt(parts[0]);
                List<Integer> optionIds = new ArrayList<>();
                for (String opt : parts[1].split(",")) {
                    optionIds.add(Integer.parseInt(opt));
                }
                answerMap.put(qId, optionIds);
            }
        }
        return answerMap;
    }

    /**
     * Checks the user's status and returns their user ID if active.
     *
     * @param userEntity
     *            the user audit to check
     * @return the user ID if the user is active
     * @throws AppException
     *             if the user authentication is inactive or deleted
     */
    public UUID checkUserStatus(UserEntity userEntity) {
        return switch (userEntity.getUserStatus()) {
            case ACTIVE -> userEntity.getUserId();
            case INACTIVE, DELETED -> throw AppUtils.Messages.USER_ACCOUNT_DEACTIVATED.getException();
            default -> throw AppUtils.Messages.USER_NOT_FOUND.getException();
        };
    }

    /**
     * Get user information by user ID.
     *
     * @param userEntity
     *            the UserEntity object containing user details
     * @return ResponseEntity containing UserResponseDto with user details
     */
    public UserResponseDto getUserDto(UserEntity userEntity) {
        UserResponseDto userResponseDto = AppUtils.map(userEntity, UserResponseDto.class);
        userResponseDto.setOrgName(organizationService.getOrgNameByOrgId(userEntity.getOrgId()));
        userResponseDto
                .setOrgIconStorageUrl(organizationService.getOrgIconStorageIdToSignedIconUrl(userEntity.getOrgId()));
        userResponseDto.setUserIconStorageUrl(storageService.getStorageUrl(userEntity.getUserIconStorageId()));
        userResponseDto.setCreatedByName(getUserNameById(userEntity.getCreatedBy()));
        userResponseDto.setUpdatedByName(getUserNameById(userEntity.getUpdatedBy()));
        return userResponseDto;
    }

    public Map<UUID, String> getUserData(List<? extends Auditable> auditableList) {
        return getUserNamesByIds(auditableList.stream()
                .flatMap(auditable -> Stream.of(auditable.getCreatedBy(), auditable.getUpdatedBy()))
                .filter(Objects::nonNull).distinct().toList());
    }

    public UserEntity createUserFromKeycloak(Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        String userMobile = jwt.getClaimAsString("mobile");
        String userId = jwt.getClaimAsString("sub");
        UserEntity userEntity = UserEntity.builder().userId(UUID.fromString(userId)).userFirstName(firstName)
                .userLastName(lastName).userEmail(userEmail).userMobile(userMobile)
                .userType(AppUtils.UserType.KEYCLOAK_USER).userStatus(AppUtils.UserStatus.ACTIVE)
                .orgId(UUID.fromString(appProperties.getDefaultOrganizationId()))
                .createdBy(UUID.fromString(appProperties.getDefaultUserId()))
                .updatedBy(UUID.fromString(appProperties.getDefaultUserId())).gender(AppUtils.Gender.PREFER_NOT_TO_SAY)
                .bloodGroup(AppUtils.BloodGroup.B_POSITIVE).skipAudit(true).build();
        return userRepository.save(userEntity);
    }
}
