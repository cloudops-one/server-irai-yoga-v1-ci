package yoga.irai.server.poem;

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
 * Controller for managing poems. Provides endpoints for adding, updating,
 * retrieving, and deleting poems.
 */
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/poems")
@Tag(name = "Poem Management", description = "APIs for managing poems")
public class PoemController {

    private final PoemService poemService;

    /**
     * Adds a new poem.
     *
     * @param poemRequestDto
     *            the request DTO containing poem details
     * @return ResponseEntity containing the response data
     */
    @PostMapping
    @Operation(summary = "Add a new poem", description = "This endpoint allows users to add a new poem.")
    public ResponseEntity<AppResponseDto<PoemResponseDto>> addPoem(@Valid @RequestBody PoemRequestDto poemRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PoemResponseDto> builder = AppResponseDto.builder();
        PoemEntity poemEntity = poemService.addPoem(poemRequestDto);
        PoemResponseDto poemResponseDto = poemService.getPoemResponseDto(poemEntity);
        return ResponseEntity
                .ok(builder.data(poemResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing poem.
     *
     * @param poemId
     *            the ID of the poem to update
     * @param poemRequestDto
     *            the request DTO containing updated poem details
     * @return ResponseEntity containing the response data
     */
    @PutMapping("/{poemId}")
    @Operation(summary = "Update an existing poem", description = "This endpoint allows users to update an existing poem.")
    public ResponseEntity<AppResponseDto<PoemResponseDto>> updatePoem(@PathVariable UUID poemId,
            @Valid @RequestBody PoemRequestDto poemRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<PoemResponseDto> builder = AppResponseDto.builder();
        PoemEntity poemEntity = poemService.updatePoem(poemId, poemRequestDto);
        PoemResponseDto poemResponseDto = poemService.getPoemResponseDto(poemEntity);
        return ResponseEntity
                .ok(builder.data(poemResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves a poem by its ID.
     *
     * @param poemId
     *            the ID of the poem to retrieve
     * @return ResponseEntity containing the response data
     */
    @GetMapping("/{poemId}")
    @Operation(summary = "Get a poem by ID", description = "This endpoint retrieves a poem by its ID.")
    public ResponseEntity<AppResponseDto<PoemResponseDto>> getPoem(@PathVariable UUID poemId) {
        AppResponseDto.AppResponseDtoBuilder<PoemResponseDto> builder = AppResponseDto.builder();
        PoemEntity poemEntity = poemService.getPoemById(poemId);
        PoemResponseDto poemResponseDto = poemService.getPoemResponseDto(poemEntity);
        return ResponseEntity
                .ok(builder.data(poemResponseDto).message(AppUtils.Messages.POEM_FOUND.getMessage()).build());
    }

    /**
     * Retrieves all poems with pagination and sorting.
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
     *            an optional keyword to filter poems by title or content
     * @return ResponseEntity containing a list of poems and pagination details
     */
    @GetMapping
    @Operation(summary = "Get all poems", description = "This endpoint retrieves all poems with pagination and sorting.")
    public ResponseEntity<AppResponseDto<List<PoemResponseDto>>> getAllPoems(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<PoemEntity> poemPage = poemService.getPoems(pageNumber, pageSize, sortBy, direction, keyword);
        List<PoemEntity> poems = poemPage.getContent();
        return ResponseEntity.ok(AppResponseDto.<List<PoemResponseDto>>builder()
                .data(poemService.toPoemResponseDto(poems)).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(poemPage, sortBy, direction)).build());
    }

    /**
     * Deletes a poem by its ID.
     *
     * @param poemId
     *            the ID of the poem to delete
     * @return ResponseEntity containing the response data
     */
    @DeleteMapping("/{poemId}")
    @Operation(summary = "Delete a poem", description = "This endpoint allows users to delete a poem by its ID.")
    public ResponseEntity<AppResponseDto<Void>> deletePoem(@PathVariable UUID poemId) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        poemService.deletePoem(poemId);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.DELETE_SUCCESS.getMessage()).build());
    }

    /**
     * Changes the status of a poem.
     *
     * @param poemId
     *            the ID of the poem to change status
     * @param status
     *            the new status to set for the poem
     * @return ResponseEntity containing the response data
     */
    @PutMapping("/status/{poemId}")
    @Operation(summary = "Change poem status", description = "This endpoint allows users to change the status of a poem.")
    public ResponseEntity<AppResponseDto<Void>> changePoemStatus(@PathVariable UUID poemId,
            @RequestParam AppUtils.PoemStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        poemService.changePoemStatus(poemId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves the total number of poems for the portal dashboard.
     *
     * @return ResponseEntity containing the total number of poems
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get portal dashboard data", description = "This endpoint retrieves the total number of poems for the portal dashboard.")
    public ResponseEntity<AppResponseDto<TotalDto>> getPortalDashboard() {
        AppResponseDto.AppResponseDtoBuilder<TotalDto> builder = AppResponseDto.builder();
        return ResponseEntity.ok(builder.data(TotalDto.builder().total(poemService.getTotalPoems()).build())
                .message(AppUtils.Messages.POEM_FOUND.getMessage()).build());
    }
}
