package yoga.irai.server.organization;

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
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.storage.StorageService;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/organization")
@Tag(name = "Organization Management", description = "APIs for managing organizations, including creation, retrieval, updating, and deletion.")
public class OrganizationController {

    private final UserService userService;
    private final StorageService storageService;
    private final OrganizationService organizationService;

    /**
     * Add a new organization
     *
     * @param organizationRequestDto
     *            the organization details to be added
     * @return ResponseEntity with ApiResponseDto containing the created
     *         organization
     */
    @PostMapping
    @Operation(summary = "Add Organization", description = "Create a new organization. Returns the created organization with its ID.")
    public ResponseEntity<AppResponseDto<OrganizationResponseDto>> addOrganization(
            @Valid @RequestBody OrganizationRequestDto organizationRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<OrganizationResponseDto> builder = AppResponseDto.builder();
        OrganizationEntity organizationEntity = organizationService.addOrganization(organizationRequestDto);
        OrganizationResponseDto organizationResponseDto = getOrganizationResponseDto(organizationEntity);
        return ResponseEntity
                .ok(builder.data(organizationResponseDto).message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Update organization by id
     *
     * @param organizationRequestDto
     *            organization details to be updated
     * @param orgId
     *            organization ID
     * @return ResponseEntity with updated organization details or error message
     */
    @PutMapping("/{orgId}")
    @Operation(summary = "Update Organization", description = "Update an existing organization by its ID. Returns the updated organization details if successful.")
    public ResponseEntity<AppResponseDto<OrganizationResponseDto>> updateOrganization(@PathVariable UUID orgId,
            @Valid @RequestBody OrganizationRequestDto organizationRequestDto) {
        AppResponseDto.AppResponseDtoBuilder<OrganizationResponseDto> builder = AppResponseDto.builder();
        OrganizationEntity organizationEntity = organizationService.updateOrganization(orgId, organizationRequestDto);
        OrganizationResponseDto organizationResponseDto = getOrganizationResponseDto(organizationEntity);
        return ResponseEntity.ok(
                builder.data(organizationResponseDto).message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Get organization by id
     *
     * @param orgId
     *            - organization ID
     * @return ResponseEntity with organization details or error message
     */
    @GetMapping("/{orgId}")
    @Operation(summary = "Get Organization by ID", description = "Retrieve an organization by its ID. Returns the organization details if found.")
    public ResponseEntity<AppResponseDto<OrganizationResponseDto>> getOrganizationById(@PathVariable UUID orgId) {
        AppResponseDto.AppResponseDtoBuilder<OrganizationResponseDto> builder = AppResponseDto.builder();
        OrganizationEntity organizationEntity = organizationService.getOrganizationById(orgId);
        OrganizationResponseDto organizationResponseDto = getOrganizationResponseDto(organizationEntity);
        return ResponseEntity.ok(builder.data(organizationResponseDto)
                .message(AppUtils.Messages.ORGANIZATION_FOUND.getMessage()).build());
    }

    /**
     * Retrieves a paginated list of organizations, optionally filtered by a search
     * keyword.
     *
     * @param pageNumber
     *            the page number
     * @param pageSize
     *            the page size
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword to filter organizations by name or
     *            description
     * @return a paginated response containing the list of organizations
     */
    @GetMapping
    @Operation(summary = "Get All Organizations", description = "Get all organizations based on a keyword. Returns a paginated list of organizations matching the keyword.")
    public ResponseEntity<AppResponseDto<List<OrganizationResponseDto>>> getOrganizations(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        AppResponseDto.AppResponseDtoBuilder<List<OrganizationResponseDto>> builder = AppResponseDto.builder();
        Page<OrganizationEntity> organizationPage = organizationService.getOrganizations(pageNumber, pageSize, sortBy,
                direction, keyword);
        List<OrganizationEntity> organizations = organizationPage.getContent();
        Set<UUID> userIds = new HashSet<>();
        Set<UUID> orgIconStorageIds = new HashSet<>();
        organizations.forEach(organization -> {
            if (organization.getCreatedBy() != null)
                userIds.add(organization.getCreatedBy());
            if (organization.getUpdatedBy() != null)
                userIds.add(organization.getUpdatedBy());
            if (organization.getOrgIconStorageId() != null)
                orgIconStorageIds.add(organization.getOrgIconStorageId());
        });
        Map<UUID, String> userNamesByIds = userService.getUserNamesByIds(userIds.stream().toList());
        Map<UUID, String> orgIconStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(orgIconStorageIds.stream().toList());
        List<OrganizationResponseDto> organizationResponseDtos = organizations.stream().map(organizationEntity -> {
            OrganizationResponseDto organizationResponseDto = AppUtils.map(organizationEntity,
                    OrganizationResponseDto.class);
            organizationResponseDto.setCreatedByName(userNamesByIds.get(organizationResponseDto.getCreatedBy()));
            organizationResponseDto.setUpdatedByName(userNamesByIds.get(organizationResponseDto.getUpdatedBy()));
            organizationResponseDto
                    .setOrgIconStorageUrl(orgIconStorageUrlByIds.get(organizationResponseDto.getOrgIconStorageId()));
            return organizationResponseDto;
        }).toList();
        builder.data(organizationResponseDtos).message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                .pageable(AppResponseDto.buildPageable(organizationPage, sortBy, direction));
        return ResponseEntity.ok(builder.build());
    }

    /**
     * Retrieves a paginated list of organizations for dropdown selection,
     * optionally filtered by a search keyword.
     *
     * @param pageNumber
     *            the page number
     * @param pageSize
     *            the page size
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword to filter organizations by name or
     *            registration number
     * @return a paginated response containing the list of organization dropdowns
     */
    @GetMapping("/dropdown")
    @Operation(summary = "Get Organization Dropdown", description = "Get a paginated list of organizations for dropdown selection, optionally filtered by a search keyword.")
    public ResponseEntity<AppResponseDto<List<OrganizationDropdownDto>>> getOrganizationDropdown(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        AppResponseDto.AppResponseDtoBuilder<List<OrganizationDropdownDto>> builder = AppResponseDto.builder();
        Page<OrganizationDropdownDto> organizationDropdownDtoPage = organizationService
                .getOrganizationDropdown(pageNumber, pageSize, sortBy, direction, keyword);
        return ResponseEntity.ok(builder.data(organizationDropdownDtoPage.getContent())
                .pageable(AppResponseDto.Pageable.builder().pageNumber(organizationDropdownDtoPage.getNumber())
                        .pageSize(organizationDropdownDtoPage.getSize())
                        .totalPages(organizationDropdownDtoPage.getTotalPages())
                        .totalElements(organizationDropdownDtoPage.getTotalElements()).sortBy(sortBy)
                        .sortDirection(direction.name()).build()

                ).message(AppUtils.Messages.SEARCH_FOUND.getMessage()).build());
    }

    /**
     * Update organization status by id
     *
     * @param orgId
     *            organization ID
     * @param status
     *            new status to be set
     */
    @PutMapping("/status/{orgId}")
    @Operation(summary = "Update Organization Status", description = "Update the status of an organization by its ID. Returns the updated organization details if successful.")
    public ResponseEntity<AppResponseDto<Void>> updateOrganizationStatus(@PathVariable UUID orgId,
            @RequestParam AppUtils.OrganizationStatus status) {
        AppResponseDto.AppResponseDtoBuilder<Void> builder = AppResponseDto.builder();
        organizationService.updateOrganizationStatus(orgId, status);
        return ResponseEntity.ok(builder.message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * get Total number of organization
     *
     * @return ResponseEntity with total number of organizations success message or
     *         error message
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get Portal Dashboard", description = "Retrieve the total number of organizations in the portal dashboard.")
    public ResponseEntity<AppResponseDto<TotalDto>> getPortalDashboard() {
        AppResponseDto.AppResponseDtoBuilder<TotalDto> builder = AppResponseDto.builder();
        return ResponseEntity
                .ok(builder.data(TotalDto.builder().total(organizationService.getTotalOrganizations()).build())
                        .message(AppUtils.Messages.ORGANIZATION_FOUND.getMessage()).build());
    }

    /**
     * @param organizationEntity
     *            - the organization audit to be converted
     * @return converts OrganizationEntity to OrganizationResponseDto
     */
    private OrganizationResponseDto getOrganizationResponseDto(OrganizationEntity organizationEntity) {
        OrganizationResponseDto organizationResponseDto = AppUtils.map(organizationEntity,
                OrganizationResponseDto.class);
        organizationResponseDto
                .setOrgIconStorageUrl(storageService.getStorageUrl(organizationEntity.getOrgIconStorageId()));
        organizationResponseDto.setCreatedByName(userService.getUserNameById(organizationEntity.getCreatedBy()));
        organizationResponseDto.setUpdatedByName(userService.getUserNameById(organizationEntity.getUpdatedBy()));
        return organizationResponseDto;
    }
}
