package yoga.irai.server.poem;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils;

@Repository
public interface PoemRepository extends JpaRepository<PoemEntity, UUID> {

    /**
     * Search for poems based on keyword, organization IDs, and status.
     *
     * @param keyword
     *            the keyword to search for in poem name, description, text, or
     *            author
     * @param orgIds
     *            the set of organization IDs to filter poems by
     * @param status
     *            the status of the poems to filter by
     * @param pageable
     *            the pagination information
     * @return a page of poems matching the search criteria
     */
    @Query("SELECT p FROM PoemEntity p WHERE (:orgIds IS NULL OR p.orgId IN :orgIds) AND "
            + "(:status IS NULL OR p.poemStatus = :status) AND"
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(p.poemName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.poemDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.poemText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.poemAuthor) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<PoemEntity> search(String keyword, Set<UUID> orgIds, AppUtils.PoemStatus status, Pageable pageable);

    /**
     * Retrieves a paginated list of poems for a specific organization.
     *
     * @param poemName
     *            - the name of the poem to search for
     * @return - true if a poem with the specified name exists, false otherwise
     */
    boolean existsByPoemName(String poemName);

    /**
     * Counts the total number of poems for a specific organization.
     *
     * @param orgId
     *            - the ID of the organization
     * @return - the total number of poems for the specified organization
     */
    Long countByOrgId(UUID orgId);

    /**
     * Retrieves a paginated list of poems for a specific organization.
     *
     * @param principalOrgId
     *            - the ID of the organization
     *
     * @param poemStatus
     *            - the status of the poems to filter by
     *
     * @return - a list of poems for the specified organization
     */
    List<PoemEntity> getTop3ByOrgIdAndPoemStatusOrderByCreatedAtDesc(UUID principalOrgId,
            AppUtils.PoemStatus poemStatus);
}
