package yoga.irai.server.practice.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.storage.StorageService;

/**
 * Controller for managing practice categories.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/practice/category")
@Tag(name = "Practice Category Management", description = "APIs fo managing practice categories including creation, retrieval, updating, and deletion.")
public class PracticeCategoryController {

    private final PracticeCategoryService practiceCategoryService;
    private final StorageService storageService;
    private final UserService userService;

    /**
     * Constructor for PracticeCategoryController.
     *
     * @param practiceCategoryRequestDto
     *            the service for managing practice categories
     * @return ResponseEntity with added practice category details
     */
    @PostMapping
    @Operation(summary = "Add Practice Category", description = "Create a new practice category, Return created practice category with its ID")
    public ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> addPracticeCategory(
            @Valid @RequestBody PracticeCategoryRequestDto practiceCategoryRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PracticeCategoryResponseDto> builder = AppResponseDto.builder();
        PracticeCategoryEntity practiceCategoryEntity = practiceCategoryService
                .addPracticeCategory(practiceCategoryRequestDto);
        PracticeCategoryResponseDto practiceCategoryResponseDto = getPracticeCategoryResponseDto(
                practiceCategoryEntity);
        return ResponseEntity.ok(
                builder.data(practiceCategoryResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Update an existing practice category by its ID.
     *
     * @param categoryId
     *            the ID of the practice category to update
     * @param practiceCategoryRequestDto
     *            the request body containing updated practice category details
     * @return ResponseEntity with updated practice category details
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "Update Practice Category", description = "Update an existing practice category by its ID. Returns the updated practice category details if successful.")
    public ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> updatePracticeCategory(
            @PathVariable UUID categoryId, @RequestBody PracticeCategoryRequestDto practiceCategoryRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PracticeCategoryResponseDto> builder = AppResponseDto.builder();
        PracticeCategoryEntity practiceCategoryEntity = practiceCategoryService.updatePracticeCategory(categoryId,
                practiceCategoryRequestDto);
        PracticeCategoryResponseDto practiceCategoryResponseDto = getPracticeCategoryResponseDto(
                practiceCategoryEntity);
        return ResponseEntity.ok(builder.data(practiceCategoryResponseDto)
                .message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Delete a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the practice category to delete
     * @return ResponseEntity with success message
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get Practice category by ID", description = "Retrieve an practice category by its ID. Returns the practice category details if found.")
    public ResponseEntity<AppResponseDto<PracticeCategoryResponseDto>> getPracticeCategoryById(
            @PathVariable UUID categoryId) {
        AppResponseDto.AppResponseDtoBuilder<PracticeCategoryResponseDto> builder = AppResponseDto.builder();
        PracticeCategoryEntity practiceCategoryEntity = practiceCategoryService.getPracticeCategoryById(categoryId);
        PracticeCategoryResponseDto practiceCategoryResponseDto = getPracticeCategoryResponseDto(
                practiceCategoryEntity);
        return ResponseEntity.ok(builder.data(practiceCategoryResponseDto)
                .message(AppUtils.Messages.PRACTICE_CATEGORY_FOUND.getMessage()).build());
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete Practice Category", description = "Delete a practice category by its ID. Returns a success message if the deletion is successful.")
    public ResponseEntity<AppResponseDto<Void>> deletePracticeCategory(@PathVariable UUID categoryId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceCategoryService.deletePracticeCategory(categoryId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Delete a practice category by its ID.
     *
     * @param pageNumber
     *            the ID of the practice category to delete
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional keyword to filter practice categories
     *
     * @return ResponseEntity with success message
     */
    @GetMapping
    @Operation(summary = "Get All Practice Category", description = "Get all practice category based on a keyword. Returns a paginated list of practice category matching the keyword.")
    public ResponseEntity<AppResponseDto<List<PracticeCategoryResponseDto>>> getPracticeCategories(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        AppResponseDto.AppResponseDtoBuilder<List<PracticeCategoryResponseDto>> builder = AppResponseDto.builder();
        Page<PracticeCategoryEntity> practiceCategoryPage = practiceCategoryService.getPracticeCategories(pageNumber,
                pageSize, sortBy, direction, keyword);
        List<PracticeCategoryEntity> practiceCategories = practiceCategoryPage.getContent();
        Set<UUID> userIds = new HashSet<>();
        Set<UUID> practiceCategoryIconStorageIds = new HashSet<>();
        practiceCategories.forEach(practiceCategory -> {
            if (practiceCategory.getCreatedBy() != null)
                userIds.add(practiceCategory.getCreatedBy());
            if (practiceCategory.getUpdatedBy() != null)
                userIds.add(practiceCategory.getUpdatedBy());
            if (practiceCategory.getPracticeCategoryIconStorageId() != null)
                practiceCategoryIconStorageIds.add(practiceCategory.getPracticeCategoryIconStorageId());
        });
        Map<UUID, String> userNamesByIds = userService.getUserNamesByIds(userIds.stream().toList());
        Map<UUID, String> practiceCategoryIconStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(practiceCategoryIconStorageIds.stream().toList());
        List<PracticeCategoryResponseDto> organizationResponseDtos = practiceCategories.stream()
                .map(practiceCategoryEntity -> {
                    PracticeCategoryResponseDto practiceCategoryResponseDto = AppUtils.map(practiceCategoryEntity,
                            PracticeCategoryResponseDto.class);
                    practiceCategoryResponseDto
                            .setCreatedByName(userNamesByIds.get(practiceCategoryResponseDto.getCreatedBy()));
                    practiceCategoryResponseDto
                            .setUpdatedByName(userNamesByIds.get(practiceCategoryResponseDto.getUpdatedBy()));
                    if (!ObjectUtils.isEmpty(practiceCategoryEntity.getPracticeCategoryIconStorageId())) {
                        practiceCategoryResponseDto
                                .setPracticeCategoryIconStorageUrl(practiceCategoryIconStorageUrlByIds
                                        .get(practiceCategoryResponseDto.getPracticeCategoryIconStorageId()));
                    }
                    return practiceCategoryResponseDto;
                }).toList();
        builder.data(organizationResponseDtos).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(practiceCategoryPage, sortBy, direction));
        return ResponseEntity.ok(builder.build());
    }

    /**
     * Get a paginated list of practice categories for dropdown selection.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of items per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional keyword to filter practice categories
     * @return ResponseEntity with a paginated list of practice categories for
     *         dropdown selection
     */
    @GetMapping("/dropdown")
    @Operation(summary = "Get Practice Category Dropdown", description = "Get a paginated list of practice categories for dropdown selection based on a keyword. Returns a paginated list of practice categories matching the keyword.")
    public ResponseEntity<AppResponseDto<List<PracticeCategoryDropdownDto>>> getPracticeCategoryDropdown(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        AppResponseDto.AppResponseDtoBuilder<List<PracticeCategoryDropdownDto>> builder = AppResponseDto.builder();
        Page<PracticeCategoryDropdownDto> practiceCategoryPage = practiceCategoryService
                .getPracticeCategoryDropdown(pageNumber, pageSize, sortBy, direction, keyword);
        return ResponseEntity.ok(builder.data(practiceCategoryPage.getContent())
                .pageable(AppResponseDto.buildPageable(practiceCategoryPage, sortBy, direction))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Update the status of a practice category by its ID.
     *
     * @param categoryId
     *            the ID of the practice category to update
     * @param status
     *            the new status to set for the practice category
     * @return ResponseEntity with success message
     */
    @PutMapping("/status/{categoryId}")
    @Operation(summary = "Update Practice Category Status", description = "Update the status of an organization by its ID. Returns the updated organization details if successful.")
    public ResponseEntity<AppResponseDto<Void>> updatePracticeCategoryStatus(@PathVariable UUID categoryId,
            @RequestParam AppUtils.PracticeCategoryStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceCategoryService.updatePracticeCategoryStatus(categoryId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Get a PracticeCategoryResponseDto from a PracticeCategoryEntity.
     *
     * @param practiceCategoryEntity
     *            the PracticeCategoryEntity to convert
     * @return PracticeCategoryResponseDto containing the details of the practice
     *         category
     */
    private PracticeCategoryResponseDto getPracticeCategoryResponseDto(PracticeCategoryEntity practiceCategoryEntity) {
        PracticeCategoryResponseDto practiceCategoryResponseDto = AppUtils.map(practiceCategoryEntity,
                PracticeCategoryResponseDto.class);
        if (!ObjectUtils.isEmpty(practiceCategoryEntity.getPracticeCategoryIconStorageId())) {
            practiceCategoryResponseDto.setPracticeCategoryIconStorageUrl(
                    storageService.getStorageUrl(practiceCategoryEntity.getPracticeCategoryIconStorageId()));
        }
        practiceCategoryResponseDto
                .setCreatedByName(userService.getUserNameById(practiceCategoryEntity.getCreatedBy()));
        practiceCategoryResponseDto
                .setUpdatedByName(userService.getUserNameById(practiceCategoryEntity.getUpdatedBy()));
        return practiceCategoryResponseDto;
    }
}
