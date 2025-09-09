package yoga.irai.server.program.section;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SectionRepository extends JpaRepository<SectionEntity, UUID> {

    /**
     * Check if a section exists by its name.
     *
     * @param sectionName
     *            the name of the section to check
     * @return true if the section exists, false otherwise
     */
    boolean existsBySectionName(String sectionName);

    /**
     * Retrieve a list of sections by program ID.
     *
     * @param programId
     *            the ID of the program to filter sections
     * @return a list of SectionEntity matching the program ID
     */
    List<SectionEntity> getAllByProgramId(UUID programId);

    /**
     * Finds the maximum section order for a given program ID.
     *
     * @param programId
     *            the ID of the program to filter sections
     * @return the maximum section order for the specified program ID, or null if no
     *         sections exist
     */
    @Query("SELECT MAX(s.sectionOrder) FROM SectionEntity s WHERE s.programId = :programId")
    Integer findMaxSectionOrderByProgramId(@Param("programId") UUID programId);
}
