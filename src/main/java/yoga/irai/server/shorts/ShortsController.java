package yoga.irai.server.shorts;

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
 * This controller handles operations related to shorts in the application. It
 * provides endpoints for adding, updating, retrieving, and deleting shorts, as
 * well as changing their status and view/like counts.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/shorts")
@Tag(name = "Shorts Management", description = "APIs for managing shorts, including creation, retrieval, updating, and deletion.")
public class ShortsController {
    private final ShortsService shortsService;

    /**
     * Adds a new shorts entry.
     *
     * @param shortsRequestDto
     *            the request DTO containing details of the shorts to be added
     * @return a response audit containing the added shorts details
     */
    @PostMapping
    @Operation(summary = "Add a new shorts", description = "Creates a new shorts entry with the provided details.")
    public ResponseEntity<AppResponseDto<ShortsResponseDto>> addShorts(
            @Valid @RequestBody ShortsRequestDto shortsRequestDto) {
        ShortsEntity shortsEntity = shortsService.addShorts(shortsRequestDto);
        ShortsResponseDto shortsResponseDto = shortsService.getShortsResponseDto(shortsEntity);
        return ResponseEntity.ok(AppResponseDto.<ShortsResponseDto>builder().data(shortsResponseDto)
                .message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing shorts entry.
     *
     * @param shortsId
     *            the ID of the shorts to be updated
     * @param shortsRequestDto
     *            the request DTO containing updated details of the shorts
     * @return a response audit containing the updated shorts details
     */
    @PutMapping("/{shortsId}")
    @Operation(summary = "Update an existing shorts", description = "Updates the details of an existing shorts entry.")
    public ResponseEntity<AppResponseDto<ShortsResponseDto>> updateShorts(@PathVariable UUID shortsId,
            @Valid @RequestBody ShortsRequestDto shortsRequestDto) {
        ShortsEntity shortsEntity = shortsService.updateShorts(shortsId, shortsRequestDto);
        ShortsResponseDto shortsResponseDto = shortsService.getShortsResponseDto(shortsEntity);
        return ResponseEntity.ok(AppResponseDto.<ShortsResponseDto>builder().data(shortsResponseDto)
                .message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a shorts entry by its ID.
     *
     * @param shortsId
     *            the ID of the shorts to be retrieved
     * @return a response audit containing the shorts details
     */
    @GetMapping("/{shortsId}")
    @Operation(summary = "Get shorts by ID", description = "Retrieves a shorts entry by its unique identifier.")
    public ResponseEntity<AppResponseDto<ShortsResponseDto>> getShorts(@PathVariable UUID shortsId) {
        ShortsEntity shortsEntity = shortsService.getShortsById(shortsId);
        ShortsResponseDto shortsResponseDto = shortsService.getShortsResponseDto(shortsEntity);
        return ResponseEntity.ok(AppResponseDto.<ShortsResponseDto>builder().data(shortsResponseDto)
                .message(AppUtils.Messages.SHORTS_FOUND.getMessage()).build());
    }

    /**
     * Retrieves a paginated list of shorts entries.
     *
     * @param pageNumber
     *            the page number to retrieve
     * @param pageSize
     *            the number of entries per page
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword
     * @return a response audit containing a list of shorts entries
     */
    @GetMapping
    @Operation(summary = "Get list of shorts", description = "Retrieves a paginated list of shorts entries, optionally filtered by a search keyword.")
    public ResponseEntity<AppResponseDto<List<ShortsResponseDto>>> getShortsList(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<ShortsEntity> shortsPage = shortsService.getShorts(pageNumber, pageSize, sortBy, direction, keyword);
        List<ShortsEntity> shortsEntities = shortsPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<ShortsResponseDto>>builder()
                .data(shortsService.toShortsResponseDto(shortsEntities))
                .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(shortsPage, sortBy, direction)).build());
    }

    /**
     * Deletes a shorts entry by its ID.
     *
     * @param shortsId
     *            the ID of the shorts to be deleted
     * @return a response audit indicating the result of the deletion
     */
    @DeleteMapping("/{shortsId}")
    @Operation(summary = "Delete shorts by ID", description = "Deletes a shorts entry by its unique identifier.")
    public ResponseEntity<AppResponseDto<Void>> deleteShorts(@PathVariable UUID shortsId) {
        shortsService.deleteShorts(shortsId);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Changes the status of a Shorts entry.
     *
     * @param shortsId
     *            the ID of the shorts to change status
     * @param status
     *            the new status to set for the shorts
     * @return a response audit indicating the result of the status change
     */
    @PutMapping("/status/{shortsId}")
    @Operation(summary = "Change shorts status", description = "Updates the status of a shorts entry.")
    public ResponseEntity<AppResponseDto<Void>> changeShortStatus(@PathVariable UUID shortsId,
            @RequestParam AppUtils.ShortsStatus status) {
        shortsService.changeShortsStatus(shortsId, status);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a dashboard view of the portal, including total shorts count.
     *
     * @return a response audit listing the total number of shorts
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get portal dashboard", description = "Retrieves the total number of shorts in the portal.")
    public ResponseEntity<AppResponseDto<TotalDto>> getPortalDashboard() {
        return ResponseEntity.ok(AppResponseDto.<TotalDto>builder()
                .data(TotalDto.builder().total(shortsService.getTotalShorts()).build())
                .message(AppUtils.Messages.PRACTICE_FOUND.getMessage()).build());
    }
}
