package yoga.irai.server.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.dto.*;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.storage.StorageService;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users, including creation, retrieval, updating, and deletion.")
public class UserController {

    private final UserService userService;
    private final StorageService storageService;
    private final OrganizationService organizationService;

    /**
     * Creates a new user with the provided registration details.
     *
     * @param userCreationRequestDto
     *            the DTO containing user registration details
     * @return a response audit containing the created user audit
     */
    @PostMapping
    @Operation(summary = "Create User", description = "Create a new user with the provided registration details.")
    public ResponseEntity<AppResponseDto<UserResponseDto>> addUser(
            @Valid @RequestBody UserCreationRequestDto userCreationRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<UserResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService.addUser(userCreationRequestDto);
        UserResponseDto userResponseDto = userService.getUserDto(userEntity);
        return ResponseEntity
                .ok(builder.data(userResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing user by their ID.
     *
     * @param userId
     *            the ID of the user to update
     * @param userRequestDto
     *            the DTO containing updated user details
     * @return a response audit containing the updated user audit if successful, or
     *         an error message if not found
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update User", description = "Update an existing user by their ID. Returns the updated user audit if successful, or an error message if not found.")
    public ResponseEntity<AppResponseDto<UserResponseDto>> updateUser(@PathVariable UUID userId,
            @Valid @RequestBody UserRequestDto userRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<UserResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService.updateUser(userId, userRequestDto);
        UserResponseDto userResponseDto = userService.getUserDto(userEntity);
        return ResponseEntity
                .ok(builder.data(userResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }
    /**
     * Retrieves a user by their ID.
     *
     * @param userId
     *            the ID of the user to retrieve
     * @return a response audit containing the user audit if found, or an error
     *         message if not found
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get User by ID", description = "Retrieve a user by their ID. Returns the user audit if found, or an error message if not found.")
    public ResponseEntity<AppResponseDto<UserResponseDto>> getUserById(@PathVariable UUID userId) {
        AppResponseDto.AppResponseDtoBuilder<UserResponseDto> builder = AppResponseDto.builder();
        UserEntity userEntity = userService.getUserById(userId);
        UserResponseDto userResponseDto = userService.getUserDto(userEntity);
        return ResponseEntity
                .ok(builder.data(userResponseDto).message(AppUtils.Messages.USER_FOUND.getMessage()).build());
    }

    /**
     * Searches for users based on the provided keyword.
     *
     * @param pageNumber
     *            the page number (default 0)
     * @param pageSize
     *            the page size (default 10)
     * @param sortBy
     *            the field to sort by (default "createdAt")
     * @param direction
     *            the sort direction (default "ASC")
     * @param keyword
     *            the search keyword (optional)
     * @return a response audit containing the user page response DTO
     */
    @GetMapping
    @Operation(summary = "Get all Users", description = "Get all users based on a keyword. Returns a paginated list of users matching the keyword.")
    public ResponseEntity<AppResponseDto<List<UserResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        AppResponseDto.AppResponseDtoBuilder<List<UserResponseDto>> builder = AppResponseDto.builder();
        Page<UserEntity> userPage = userService.getUsers(pageNumber, pageSize, sortBy, direction, keyword);
        List<UserEntity> users = userPage.getContent();
        Set<UUID> userIds = new HashSet<>();
        Set<UUID> orgIds = new HashSet<>();
        Set<UUID> userIconStorageIds = new HashSet<>();
        users.forEach(user -> {
            if (user.getCreatedBy() != null)
                userIds.add(user.getCreatedBy());
            if (user.getUpdatedBy() != null)
                userIds.add(user.getUpdatedBy());
            if (user.getOrgId() != null)
                orgIds.add(user.getOrgId());
            if (user.getUserIconStorageId() != null)
                userIconStorageIds.add(user.getUserIconStorageId());
        });
        Map<UUID, String> userNamesByIds = userService.getUserNamesByIds(userIds.stream().toList());
        Map<UUID, String> orgNameByIds = organizationService.getOrgNamesByIds(orgIds.stream().toList());
        Map<UUID, String> userIconStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(userIconStorageIds.stream().toList());
        List<UserResponseDto> userResponseDtos = users.stream().map(userEntity -> {
            UserResponseDto userResponseDto = AppUtils.map(userEntity, UserResponseDto.class);
            userResponseDto.setCreatedByName(userNamesByIds.get(userResponseDto.getCreatedBy()));
            userResponseDto.setUpdatedByName(userNamesByIds.get(userResponseDto.getUpdatedBy()));
            userResponseDto.setOrgName(orgNameByIds.get(userResponseDto.getOrgId()));
            userResponseDto.setUserIconStorageUrl(userIconStorageUrlByIds.get(userResponseDto.getUserIconStorageId()));
            return userResponseDto;
        }).toList();
        builder.data(userResponseDtos).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(userPage, sortBy, direction));
        return ResponseEntity.ok(builder.build());
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId
     *            the ID of the user to delete
     * @return a response audit containing a success message if deletion is
     *         successful, or an error message if not found
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete User", description = "Delete a user by their ID. Returns a success message if deletion is successful, or an error message if not found.")
    public ResponseEntity<AppResponseDto<Void>> deleteUser(@PathVariable UUID userId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.updateUserStatus(userId, AppUtils.UserStatus.DELETED);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage(userId)).build());
    }

    /**
     * Updates the status of a user by their ID.
     *
     * @param userId
     *            the ID of the user to update
     * @param status
     *            the new status to set for the user
     * @return a response audit containing a success message if the update is
     *         successful, or an error message if not found
     */
    @PutMapping("/status/{userId}")
    @Operation(summary = "Update Organization Status", description = "Update the status of an organization by its ID. Returns the updated organization details if successful.")
    public ResponseEntity<AppResponseDto<Void>> updateUserStatus(@PathVariable UUID userId,
            @RequestParam AppUtils.UserStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage(userId)).build());
    }

    @GetMapping("/profile/picture")
    @Operation(summary = "Get User Profile Picture", description = "Retrieve the URL of the user's profile picture.")
    public ResponseEntity<AppResponseDto<UserIconStorageUrlDto>> getUserIconStorageUrl() {
        AppResponseDto.AppResponseDtoBuilder<UserIconStorageUrlDto> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder
                .data(UserIconStorageUrlDto.builder()
                        .userIconStorageUrl(storageService.getStorageUrl(
                                userService.getUserById(AppUtils.getPrincipalUserId()).getUserIconStorageId()))
                        .build())
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    @GetMapping("/aoi/{userId}")
    @Operation(summary = "Update Organization Status", description = "Update the status of an organization by its ID. Returns the updated organization details if successful.")
    public ResponseEntity<AppResponseDto<List<UserAoiDto>>> updateUserStatus(@PathVariable UUID userId) {
        AppResponseDto.AppResponseDtoBuilder<List<UserAoiDto>> builder = AppResponseDto.builder();
        List<UserAoiDto> userAoiDtos = userService.getUserAoi(userService.getUserById(userId));
        return ResponseEntity
                .ok(builder.data(userAoiDtos).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage(userId)).build());
    }

    /**
     * updates the password of a user by their ID
     *
     * @param userId
     *            the ID of the user to update
     * @param updatePasswordDto
     *            the old and new password to be updated.
     *
     */
    @PutMapping("reset/password/{userId}")
    @Operation(summary = "Reset User Password", description = "Reset the password of a user by their ID. Returns a success message if the reset is successful, or an error message if not found.")
    public ResponseEntity<AppResponseDto<Void>> resetUserPassword(@PathVariable UUID userId,
            @RequestBody @Valid UpdatePasswordDto updatePasswordDto) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        userService.resetUserPassword(userId, updatePasswordDto);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.PASSWORD_RESET_SUCCESS.getMessage(userId)).build());
    }

    /**
     * Retrieves the user statistics for the dashboard.
     *
     * @return a response audit containing the user statistics DTO
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get User Statistics for Dashboard", description = "Retrieve user statistics for the dashboard. Returns the user statistics DTO.")
    public ResponseEntity<AppResponseDto<UserStatsDto>> dashboard() {
        AppResponseDto.AppResponseDtoBuilder<UserStatsDto> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(userService.getUserStats())
                .message(AppUtils.Messages.USER_STATS_FOUND.getMessage()).build());
    }
}
