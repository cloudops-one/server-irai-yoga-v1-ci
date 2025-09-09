package yoga.irai.server.practice.category;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yoga.irai.server.mobile.dto.PracticeCategoryListDto;

@Repository
public interface PracticeCategoryRepository extends JpaRepository<PracticeCategoryEntity, UUID> {

    /**
     * Searches for practice categories by keyword.
     *
     * @param keyword
     *            - Search keyword for category names
     * @param pageable
     *            - Pageable object for pagination
     * @return Page of PracticeCategoryEntity
     */
    @Query("SELECT p FROM PracticeCategoryEntity p WHERE LOWER(p.practiceCategoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PracticeCategoryEntity> search(String keyword, Pageable pageable);

    /**
     * Finds all practice categories by their status.
     *
     * @param practiceCategoryIds
     *            - List of category IDs
     * @return List of PracticeCategoryEntity
     */
    List<PracticeCategoryDropdownDto> findPracticeCategoryIconStorageIdByPracticeCategoryIdIn(
            List<UUID> practiceCategoryIds);

    /**
     * Retrieves a paginated list of practice categories for dropdowns.
     *
     * @param pageable
     *            - Pageable object for pagination
     * @return Page of PracticeCategoryDropdownDto
     */
    @Query("SELECT p.practiceCategoryId as practiceCategoryId,p.practiceCategoryName as practiceCategoryName FROM PracticeCategoryEntity p")
    Page<PracticeCategoryDropdownDto> getDropdown(Pageable pageable);

    /**
     * Searches for practice categories by keyword and returns a paginated list of
     * dropdown DTOs.
     *
     * @param keyword
     *            - Search keyword for category names
     * @param pageable
     *            - Pageable object for pagination
     * @return Page of PracticeCategoryDropdownDto
     */
    @Query("SELECT p.practiceCategoryId as practiceCategoryId, p.practiceCategoryName as practiceCategoryName FROM PracticeCategoryEntity p WHERE "
            + "LOWER(p.practiceCategoryName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PracticeCategoryDropdownDto> getDropdownSearch(String keyword, Pageable pageable);

    /**
     * Retrieves a paginated list of practice categories for dropdowns.
     *
     * @return Page of PracticeCategoryDropdownDto
     */
    @Query("SELECT p.practiceCategoryId as practiceCategoryId,p.practiceCategoryName as practiceCategoryName,  p.practiceCategoryIconStorageId as practiceCategoryIconStorageId, p.practiceCategoryIconExternalUrl as practiceCategoryIconExternalUrl  FROM PracticeCategoryEntity p")
    List<PracticeCategoryListDto> getPracticeCategoryList();

    /**
     * Retrieves a practice category audit by its ID.
     *
     * @param categoryId
     *            - ID of the practice category
     * @return PracticeCategoryEntity
     */
    PracticeCategoryEntity getPracticeCategoryEntityByPracticeCategoryId(UUID categoryId);

    /**
     * Checks if a practice category with the given name exists.
     *
     * @param practiceCategoryName
     *            - Name of the practice category
     * @return true if exists, false otherwise
     */
    boolean existsByPracticeCategoryName(String practiceCategoryName);
}
