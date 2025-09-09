package yoga.irai.server.news;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, UUID> {

    /**
     * Check if a news entity exists by its name.
     *
     * @param newsName
     *            the name of the news entity
     * @return true if a news entity with the given name exists, false otherwise
     */
    boolean existsByNewsName(String newsName);

    /**
     * Find a news entity by keyword AND isRecommended status.
     *
     * @param keyword
     *            the ID of the news entity
     * @return the news entity if found, or null if not found
     */
    @Query("SELECT n FROM NewsEntity n WHERE " + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(n.newsName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(n.newsDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<NewsEntity> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Retrieve a list of news entities filtered by their news status and ordered by
     * creation date in descending order.
     *
     * @param newsStatus
     *            the status of the news entities to filter by
     * @return a list of news entities matching the specified status, ordered by
     *         creation date descending
     */
    List<NewsEntity> getNewsEntitiesByNewsStatusOrderByCreatedAtDesc(AppUtils.NewsStatus newsStatus);

    /**
     * Retrieve the top 3 news entities filtered by their news status and ordered by
     * creation date in descending order.
     *
     * @param newsStatus
     *            the status of the news entities to filter by
     * @return a list of the top 3 news entities matching the specified status,
     *         ordered by creation date descending
     */
    List<NewsEntity> findTop3ByNewsStatusOrderByCreatedAtDesc(AppUtils.NewsStatus newsStatus);
}
