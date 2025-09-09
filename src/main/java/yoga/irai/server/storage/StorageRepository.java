package yoga.irai.server.storage;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StorageRepository extends JpaRepository<StorageEntity, UUID> {

    /**
     * Searches for StorageEntity objects where the tags contain the specified
     * keyword.
     *
     * @param keyword
     *            the keyword to search for in the tags
     * @param pageable
     *            pagination information
     * @return a list of StorageEntity objects that match the search criteria
     */
    @Query("SELECT s FROM StorageEntity s WHERE LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StorageEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO storage (storage_id, content_type, extension, size, tags, created_by) "
            + "VALUES (:storageId, :contentType, :extension, :size, :tags, :createdBy)", nativeQuery = true)
    void insertStorage(@Param("storageId") UUID storageId, @Param("contentType") String contentType,
            @Param("extension") String extension, @Param("size") Long size, @Param("tags") String tags,
            @Param("createdBy") UUID createdBy);
}
