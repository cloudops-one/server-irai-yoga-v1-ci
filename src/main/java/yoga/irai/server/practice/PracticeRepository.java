package yoga.irai.server.practice;

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
public interface PracticeRepository extends JpaRepository<PracticeEntity, UUID> {

    /**
     * Finds a practice by its ID.
     *
     * @param categoryId
     *            the ID of the practiceCategory to find
     *
     * @param keyword
     *            the keyword to search for in practice name or description
     * @param orgIds
     *            the set of organization IDs to filter practices
     * @param status
     *            the status of the practice to filter
     * @param pageable
     *            the pagination information
     *
     * @return the found PracticeEntity, or null if not found
     */
    @Query("SELECT p FROM PracticeEntity p WHERE " + "(:categoryId IS NULL OR p.practiceCategoryId = :categoryId) AND "
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE "
            + "(LOWER(p.practiceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.practiceDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) END) AND "
            + "(:orgIds IS NULL OR p.orgId IN :orgIds) AND " + "(:status IS NULL OR p.practiceStatus = :status)")
    Page<PracticeEntity> searchDynamic(UUID categoryId, String keyword, Set<UUID> orgIds,
            AppUtils.PracticeStatus status, Pageable pageable);

    /**
     * Checks if a practice with the given name exists.
     *
     * @param practiceName
     *            the name of the practice to check
     * @return true if a practice with the given name exists, false otherwise
     */
    boolean existsByPracticeName(String practiceName);

    /**
     * finds count of practices by organization ID.
     *
     * @param orgId
     *            - the ID of the organization to find practices for
     * @return the count of practices associated with the given organization ID
     */
    Long countByOrgId(UUID orgId);

    /**
     * Finds the top 3 practices by organization ID and practice status, ordered by
     * creation date in descending order.
     *
     * @param principalOrgId
     *            the ID of the organization to filter practices
     * @param status
     *            the status of the practices to filter
     * @return a list of the top 3 PracticeEntity objects matching the criteria
     */
    List<PracticeEntity> getTop3ByOrgIdAndPracticeStatusOrderByCreatedAtDesc(UUID principalOrgId,
            AppUtils.PracticeStatus status);
}
