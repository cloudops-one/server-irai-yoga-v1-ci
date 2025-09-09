package yoga.irai.server.shorts;

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
public interface ShortsRepository extends JpaRepository<ShortsEntity, UUID> {

    /**
     * Search for shorts based on keyword, organization IDs, and status.
     *
     * @param keyword
     *            the keyword to search for in shorts name or description
     * @param orgIds
     *            the set of organization IDs to filter shorts by
     * @param status
     *            the status of the shorts to filter by
     * @param pageable
     *            the pagination information
     * @return a page of shorts matching the search criteria
     */
    @Query("SELECT s FROM ShortsEntity s WHERE (:orgIds IS NULL OR s.orgId IN :orgIds) AND "
            + "(:status IS NULL OR s.shortsStatus = :status) AND"
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(s.shortsName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(s.shortsDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<ShortsEntity> search(String keyword, Set<UUID> orgIds, AppUtils.ShortsStatus status, Pageable pageable);

    /**
     * Check if a shorts with the given name exists.
     *
     * @param shortsName
     *            - the name of the shorts to check
     *
     * @return true if a shorts with the given name exists, false otherwise
     */
    boolean existsByShortsName(String shortsName);

    /**
     * Find a number of shorts by organization ID.
     *
     * @param orgId
     *            - the organization ID to search for
     *
     * @return the ShortsEntity if found, null otherwise
     */
    Long countByOrgId(UUID orgId);

    /**
     * Find the top 3 shorts by organization ID and status, ordered by creation
     * date.
     *
     * @param principalOrgId
     *            the organization ID to filter by
     * @param shortsStatus
     *            the status of the shorts to filter by
     * @return a list of the top 3 ShortsEntity matching the criteria
     */
    List<ShortsEntity> getTop3ByOrgIdAndShortsStatusOrderByCreatedAtDesc(UUID principalOrgId,
            AppUtils.ShortsStatus shortsStatus);
}
