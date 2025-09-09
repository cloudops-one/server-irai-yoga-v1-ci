package yoga.irai.server.practice;

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
import yoga.irai.server.app.dto.TotalDto;

/**
 * Controller for managing practices.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/practice")
@Tag(name = "Practice Management", description = "APIs for managing practices, including creation, retrieval, update and delete")
public class PracticeController {
    private final PracticeService practiceService;

    /**
     * @param practiceRequestDto
     *            - contains all the data required to create a practice.
     * @return ResponseEntity with Practice Entity along with PracticeId
     */
    @PostMapping
    @Operation(summary = "Add Practice", description = "Create a Practice, Return the Id along with newly added practice")
    public ResponseEntity<AppResponseDto<PracticeResponseDto>> addPractice(
            @Valid @RequestBody PracticeRequestDto practiceRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PracticeResponseDto> builder = AppResponseDto.builder();
        PracticeEntity practiceEntity = practiceService.addPractice(practiceRequestDto);
        PracticeResponseDto practiceResponseDto = practiceService.getPracticeResponseDto(practiceEntity);
        return ResponseEntity
                .ok(builder.data(practiceResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing practice.
     *
     * @param practiceId
     *            - the ID of the practice to update
     * @param practiceRequestDto
     *            - the request data containing updated practice information
     * @return ResponseEntity with updated PracticeResponseDto
     */
    @PutMapping("/{practiceId}")
    @Operation(summary = "Update Practice", description = "Update an existing practice by its ID")
    public ResponseEntity<AppResponseDto<PracticeResponseDto>> updatePractice(@PathVariable UUID practiceId,
            @Valid @RequestBody PracticeRequestDto practiceRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PracticeResponseDto> builder = AppResponseDto.builder();
        PracticeEntity practiceEntity = practiceService.updatePractice(practiceId, practiceRequestDto);
        PracticeResponseDto practiceResponseDto = practiceService.getPracticeResponseDto(practiceEntity);
        return ResponseEntity
                .ok(builder.data(practiceResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a practice by its ID.
     *
     * @param practiceId
     *            - the ID of the practice to retrieve
     * @return ResponseEntity with PracticeResponseDto containing the practice
     *         details
     */
    @GetMapping("/{practiceId}")
    @Operation(summary = "Get Practice", description = "Get a practice by its ID")
    public ResponseEntity<AppResponseDto<PracticeResponseDto>> getPractice(@PathVariable UUID practiceId) {
        AppResponseDto.AppResponseDtoBuilder<PracticeResponseDto> builder = AppResponseDto.builder();
        PracticeEntity practiceEntity = practiceService.getPracticeById(practiceId);
        PracticeResponseDto practiceResponseDto = practiceService.getPracticeResponseDto(practiceEntity);
        return ResponseEntity
                .ok(builder.data(practiceResponseDto).message(AppUtils.Messages.PRACTICE_FOUND.getMessage()).build());
    }

    /**
     * Retrieves a paginated list of practices.
     *
     * @param pageNumber
     *            - the page number to retrieve
     * @param pageSize
     *            - the number of practices per page
     * @param sortBy
     *            - the field to sort by
     * @param direction
     *            - the sort direction (ASC or DESC)
     * @param categoryId
     *            - the category ID to filter practices by
     * @return ResponseEntity with a list of PracticeResponseDto
     */
    @GetMapping
    @Operation(summary = "Get All Practices", description = "Get all practice based on a keyword. Returns a paginated list of practice  matching the practice category.")
    public ResponseEntity<AppResponseDto<List<PracticeResponseDto>>> getPractices(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) UUID categoryId) {

        Page<PracticeEntity> practicePage = practiceService.getPractices(pageNumber, pageSize, sortBy, direction,
                keyword, categoryId);
        List<PracticeEntity> practices = practicePage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<PracticeResponseDto>>builder()
                .data(practiceService.toPracticeResponseDto(practices))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(practicePage, sortBy, direction)).build());
    }

    /**
     * Deletes a practice by its ID.
     *
     * @param practiceId
     *            - the ID of the practice to delete
     * @return ResponseEntity with a success message
     */
    @DeleteMapping("/{practiceId}")
    @Operation(summary = "Delete Practice", description = "Delete a practice by its ID")
    public ResponseEntity<AppResponseDto<Void>> deletePractice(@PathVariable UUID practiceId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceService.delete(practiceId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Changes the status of a practice.
     *
     * @param practiceId
     *            - the ID of the practice to update
     * @param status
     *            - the new status to set for the practice
     * @return ResponseEntity with a success message
     */
    @PutMapping("/status/{practiceId}")
    @Operation(summary = "Change Practice Status", description = "Change the status of a practice to either ACTIVE or INACTIVE")
    public ResponseEntity<AppResponseDto<Void>> changePracticeStatus(@PathVariable UUID practiceId,
            @RequestParam AppUtils.PracticeStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        practiceService.changePracticeStatus(practiceId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves the total number of practices for the portal dashboard.
     *
     * @return ResponseEntity with TotalPracticeDto containing the total number of
     *         practices
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get Portal Dashboard", description = "Get the total number of practices for the portal dashboard")
    public ResponseEntity<AppResponseDto<TotalDto>> getPortalDashboard() {
        AppResponseDto.AppResponseDtoBuilder<TotalDto> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(TotalDto.builder().total(practiceService.getTotalPractices()).build())
                .message(AppUtils.Messages.PRACTICE_FOUND.getMessage()).build());
    }

}
