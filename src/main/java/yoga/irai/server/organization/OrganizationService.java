package yoga.irai.server.organization;

import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppProperties;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.storage.StorageService;

@Service
@AllArgsConstructor
public class OrganizationService {

    private final AppProperties appProperties;
    private final StorageService storageService;
    private final OrganizationRepository organizationRepository;

    /**
     * Adds a new organization.
     *
     * @param organizationRequestDto
     *            the organization data transfer object containing organization
     *            details
     * @return the OrganizationEntity of the newly added organization
     */
    public OrganizationEntity addOrganization(OrganizationRequestDto organizationRequestDto) {
        if (organizationRepository.existsByOrgName(organizationRequestDto.getOrgName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        OrganizationEntity organizationEntity = AppUtils.map(organizationRequestDto, OrganizationEntity.class);
        organizationEntity.setOrgId(null);
        AppUtils.updateIdsWithPrimary(organizationEntity.getAddresses());
        AppUtils.updateIdsWithPrimary(organizationEntity.getContacts());
        AppUtils.updateIds(organizationEntity.getUrls());
        return organizationRepository.save(organizationEntity);
    }

    /**
     * Updates an existing organization.
     *
     * @param orgId
     *            the organization ID as a string
     * @param organizationRequestDto
     *            the updated organization data
     * @return the updated organization audit
     */
    @Transactional
    public OrganizationEntity updateOrganization(UUID orgId, OrganizationRequestDto organizationRequestDto) {
        OrganizationEntity organizationEntity = getOrganizationByOrgId(orgId);
        if (!organizationRequestDto.getOrgIconStorageId().equals(organizationEntity.getOrgIconStorageId())) {
            storageService.deleteStorageById(organizationEntity.getOrgIconStorageId());
        }
        AppUtils.map(organizationRequestDto, organizationEntity);
        organizationEntity.setOrgId(orgId);
        AppUtils.updateIdsWithPrimary(organizationEntity.getAddresses());
        AppUtils.updateIdsWithPrimary(organizationEntity.getContacts());
        AppUtils.updateIds(organizationEntity.getUrls());
        return organizationRepository.save(organizationEntity);
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param orgId
     *            the ID of the event to retrieve
     * @return the Organization audit if found, otherwise null
     */
    private OrganizationEntity getOrganizationByOrgId(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> AppUtils.Messages.ORGANIZATION_NOT_FOUND.getException(orgId));
    }

    /**
     * Retrieves a paginated list of organizations, optionally filtered by a search
     * keyword.
     *
     * @param pageNumber
     *            the page number
     * @param pageSize
     *            the page pageSize
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction
     * @param keyword
     *            the search keyword (optional)
     * @return a paginated response DTO containing organizations
     */
    public Page<OrganizationEntity> getOrganizations(int pageNumber, int pageSize, String sortBy,
            Sort.Direction direction, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());

        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> organizationRepository.search(keyword, getOrgIdsForMobile(),
                    AppUtils.OrganizationStatus.INACTIVE, pageable);
            case PORTAL_USER -> organizationRepository.search(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> organizationRepository.search(keyword, null, null, pageable);
        };
    }

    /**
     * Retrieves an organization by its ID.
     *
     * @param orgId
     *            the ID of the organization to retrieve
     * @return the organization audit if found, otherwise null
     */
    public OrganizationEntity getOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> AppUtils.Messages.ORGANIZATION_NOT_FOUND.getException(orgId));

    }

    /**
     * Retrieves a paginated list of organizations for dropdowns, optionally
     * filtered by a search keyword.
     *
     * @param pageNumber
     *            the page number
     * @param pageSize
     *            the page size
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction
     * @param keyword
     *            the search keyword (optional)
     * @return a paginated response DTO containing organization dropdowns
     */
    public Page<OrganizationDropdownDto> getOrganizationDropdown(int pageNumber, int pageSize, String sortBy,
            Sort.Direction direction, String keyword) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());

        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> organizationRepository.getDropdownSearch(keyword, getOrgIdsForMobile(),
                    AppUtils.OrganizationStatus.INACTIVE, pageable);
            case PORTAL_USER -> organizationRepository.getDropdownSearch(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> organizationRepository.getDropdownSearch(keyword, null, null, pageable);
        };

    }

    /**
     * Get Organization Name by Organization UUID
     *
     * @param orgId
     *            orgId UUID
     * @return orgName
     */
    public String getOrgNameByOrgId(UUID orgId) {
        OrganizationEntity organizationEntity = organizationRepository.findByOrgId(orgId);
        if (organizationEntity == null) {
            return "";
        }
        return organizationEntity.getOrgName();
    }

    /**
     * Retrieves a list of organization names by their IDs.
     *
     * @param orgIds
     *            a list of organization IDs
     * @return orgIds a list of organization names
     */
    public Map<UUID, String> getOrgNamesByIds(List<UUID> orgIds) {
        return organizationRepository.findOrgNameByOrgIdIn(orgIds).stream()
                .collect(Collectors.toMap(OrganizationDropdownDto::getOrgId, OrganizationDropdownDto::getOrgName));
    }

    /**
     * Retrieves a list of organization registration numbers by their IDs.
     *
     * @param orgIds
     *            a list of organization IDs
     * @return a map of organization IDs to their registration numbers
     */
    public Map<UUID, String> getOrgIconStorageUrlByIds(List<UUID> orgIds) {
        return organizationRepository.findOrgIconStorageIdByOrgIdIn(orgIds).stream().collect(
                Collectors.toMap(OrganizationDropdownDto::getOrgId, OrganizationDropdownDto::getOrgIconStorageId));
    }

    /**
     * Deletes an organization by its ID.
     *
     * @param orgId
     *            the ID of the organization to delete
     * @param status
     *            the status to be updated
     */
    public void updateOrganizationStatus(UUID orgId, AppUtils.OrganizationStatus status) {
        OrganizationEntity organizationEntity = getOrganizationByOrgId(orgId);
        organizationEntity.setOrgStatus(status);
        organizationRepository.save(organizationEntity);
    }

    /**
     * Retrieves organization icon IDs to signed icon URL.
     *
     * @param orgId
     *            - a list of organization IDs
     * @return signed icon URLs
     */
    public String getOrgIconStorageIdToSignedIconUrl(UUID orgId) {
        OrganizationEntity organizationEntity = getOrganizationById(orgId);
        return storageService.getStorageUrl(organizationEntity.getOrgIconStorageId());
    }

    /**
     * Retrieves a map of organization icon IDs to signed icon URLs.
     *
     * @param orgIds
     *            - a list of organization IDs
     * @return a map where keys are organization IDs and values are signed icon URLs
     */
    public Map<UUID, String> getOrgIconStorageIdToSignedIconUrl(List<UUID> orgIds) {
        Map<UUID, String> orgIconStorageUrlByIds = getOrgIconStorageUrlByIds(orgIds);
        List<UUID> orgIconStorageIds = orgIconStorageUrlByIds.values().stream().filter(Objects::nonNull)
                .map(UUID::fromString).toList();
        Map<UUID, String> signedIconUrls = storageService.getSignedStorageUrlByIds(orgIconStorageIds);
        if (ObjectUtils.isEmpty(signedIconUrls)) {
            return new HashMap<>();
        }

        Map<UUID, String> orgIconStorageSignedUrlByIds = getOrgIconStorageUrlByIds(orgIds);
        for (Map.Entry<UUID, String> entry : orgIconStorageUrlByIds.entrySet()) {
            if (ObjectUtils.isEmpty(entry.getValue())) {
                orgIconStorageSignedUrlByIds.put(entry.getKey(), null);
            } else {
                orgIconStorageSignedUrlByIds.put(entry.getKey(), signedIconUrls.get(UUID.fromString(entry.getValue())));
            }
        }
        return orgIconStorageSignedUrlByIds;
    }

    /**
     * Retrieves the total number of organizations based on the user type.
     *
     * @return the total number of organizations
     */
    public Long getTotalOrganizations() {
        return switch (AppUtils.getPrincipalUserType()) {
            case KEYCLOAK_USER -> organizationRepository.count();
            case PORTAL_USER -> organizationRepository.countByOrgId(AppUtils.getPrincipalOrgId());
            case MOBILE_USER -> throw new AppException(AppUtils.Messages.ACCESS_DENIED.getMessage());
        };
    }

    /**
     * Retrieves the organization IDs for mobile users.
     *
     * @return a set of organization IDs for mobile users
     */
    public Set<UUID> getOrgIdsForMobile() {
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());
        orgIds.add(UUID.fromString(appProperties.getDefaultOrganizationId()));
        return orgIds;
    }

    public String getTopicName() {
        String orgName = getOrgNameByOrgId(AppUtils.getPrincipalOrgId());
        orgName = orgName.trim().toLowerCase().replaceAll("\\s+", "-");
        return orgName;
    }
}
