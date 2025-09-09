package yoga.irai.server.program.section.lesson;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<LessonEntity, UUID> {

    /**
     * Checks if a lesson with the given name exists.
     *
     * @param lessonName
     *            the name of the lesson to check
     * @return true if a lesson with the given name exists, false otherwise
     */
    boolean existsByLessonName(String lessonName);

    /**
     * Checks if a lesson with the given name exists in the specified section.
     *
     * @param sectionId
     *            the ID of the section to check within
     * @return list of lessons in the specified section
     */
    List<LessonEntity> getAllBySectionId(UUID sectionId);

    /**
     * Finds a lesson Order by its ID.
     *
     * @param sectionId
     *            - the ID of the section to which the lesson belongs
     * @return the maximum lesson order for the specified section
     */
    @Query("SELECT MAX(l.lessonOrder) FROM LessonEntity l WHERE l.sectionId = :sectionId")
    Integer findMaxLessonOrderBySectionId(@Param("sectionId") UUID sectionId);
}
