package yoga.irai.server.organization;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils;

/**
 * Repository interface for managing Organization entities. Provides methods to
 * perform CRUD operations and custom queries.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    /**
     * Searches for organizations by a keyword in their name or email.
     *
     * @param keyword
     *            the keyword to search for
     * @param orgIds
     *            - the set of organization IDs to filter the search (can be null to
     *            include all organizations)
     * @param status
     *            the status to filter organizations (can be null to include all
     *            statuses)
     * @param pageable
     *            pagination information
     * @return a page of organization entities that match the search criteria
     */
    @Query("SELECT o FROM OrganizationEntity o WHERE (:orgIds IS NULL OR o.orgId IN :orgIds) AND "
            + "(:status IS NULL OR NOT o.orgStatus = :status) AND"
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(o.orgName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(o.orgEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<OrganizationEntity> search(String keyword, Set<UUID> orgIds, AppUtils.OrganizationStatus status,
            Pageable pageable);

    /**
     * Retrieves a paginated list of organization dropdown DTOs based on the
     *
     * @param keyword
     *            - the search keyword to filter organizations by name or email
     * @param orgIds
     *            - the set of organization IDs to filter the search (can be null to
     *            include all organizations)
     * @param status
     *            - the status to filter organizations (can be null to include all
     *            statuses)
     * @param pageable
     *            - pagination information
     *
     * @return - a page of organization dropdown DTOs that match the search criteria
     */
    @Query("SELECT o.orgId as orgId, o.orgName as orgName, o.orgRegistrationNumber as orgRegistrationNumber FROM OrganizationEntity o "
            + "WHERE (:orgIds IS NULL OR o.orgId IN :orgIds) AND "
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(o.orgName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(o.orgEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<OrganizationDropdownDto> getDropdownSearch(String keyword, Set<UUID> orgIds,
            AppUtils.OrganizationStatus status, Pageable pageable);

    /**
     * Finds organization names by their IDs.
     *
     * @param orgIds
     *            - the list of organization IDs to search for
     * @return a list of organization dropdown DTOs containing the organization ID,
     *         name, and registration number
     */
    List<OrganizationDropdownDto> findOrgNameByOrgIdIn(List<UUID> orgIds);

    /**
     * Finds organization icon storage IDs by their IDs.
     *
     * @param orgIds
     *            - the list of organization IDs to search for
     * @return a list of organization dropdown DTOs containing the organization ID
     *         and icon storage ID
     */
    List<OrganizationDropdownDto> findOrgIconStorageIdByOrgIdIn(List<UUID> orgIds);

    /**
     * Checks if an organization exists by its name.
     *
     * @param orgName
     *            - the name of the organization to check
     * @return true if an organization with the given name exists, false otherwise
     */
    boolean existsByOrgName(String orgName);

    /**
     * Finds count of organizations by their organization ID.
     *
     * @param orgId
     *            - the organization ID to search for
     *
     * @return the count of organizations with the given organization ID
     */
    Long countByOrgId(UUID orgId);

    /**
     * Finds an organization entity by its organization ID.
     *
     * @param orgId
     *            - the organization ID to search for
     * @return the organization entity with the given organization ID, or null if
     *         not found
     */
    OrganizationEntity findByOrgId(UUID orgId);
}
