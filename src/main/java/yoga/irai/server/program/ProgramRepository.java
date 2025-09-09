package yoga.irai.server.program;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import yoga.irai.server.app.AppUtils;

public interface ProgramRepository extends JpaRepository<ProgramEntity, UUID> {

    /**
     * Check if a program exists by its name.
     *
     * @param programName
     *            the name of the program to check
     * @return true if the program exists, false otherwise
     */
    boolean existsByProgramName(String programName);

    /**
     * Retrieve a list of program by keyword, organization IDs, and status.
     *
     * @param keyword
     *            the keyword to search in program name or description
     * @param orgIds
     *            the set of organization IDs to filter programs
     * @param status
     *            the status of the program to filter by
     * @param pageable
     *            the pagination information
     * @return a page of ProgramEntity matching the criteria
     */
    @Query("SELECT p FROM ProgramEntity p WHERE " + "(:orgIds IS NULL OR p.orgId IN :orgIds) AND "
            + "(:status IS NULL OR p.programStatus = :status) AND "
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(p.programName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.programDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<ProgramEntity> search(String keyword, Set<UUID> orgIds, AppUtils.ProgramStatus status, Pageable pageable);

    /**
     * Count the number of programs by organization ID.
     *
     * @param orgId
     *            the organization ID to filter programs
     * @return number of programs matching the organization ID
     */
    Long countByOrgId(UUID orgId);

    /**
     * Retrieve the top 3 programs by organization ID and status, ordered by
     * creation date in descending order.
     *
     * @param principalOrgId
     *            the organization ID to filter programs
     * @param programStatus
     *            the status of the program to filter by
     * @return a list of ProgramEntity matching the criteria
     */
    List<ProgramEntity> getTop3ByOrgIdAndProgramStatusOrderByCreatedAtDesc(UUID principalOrgId,
            AppUtils.ProgramStatus programStatus);
}
