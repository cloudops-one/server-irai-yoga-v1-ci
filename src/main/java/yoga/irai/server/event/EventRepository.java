package yoga.irai.server.event;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yoga.irai.server.app.AppUtils;

/** Repository interface for managing Event entities. */
@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    /**
     * Finds an EventEntity by its eventId.
     *
     * @param keyword
     *            - the keyword to search in event name or description
     * @param orgIds
     *            - the set of organization IDs to filter events
     * @param status
     *            - the status to filter events
     * @param pageable
     *            - pagination information
     * @return the EventEntity if found, otherwise null
     */
    @Query("SELECT e FROM EventEntity e WHERE (:orgIds IS NULL OR e.orgId IN :orgIds) AND "
            + "(:status IS NULL OR NOT e.eventStatus = :status) AND"
            + "(CASE WHEN :keyword IS NULL OR :keyword = '' THEN true ELSE( "
            + "LOWER(e.eventName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(e.eventDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) END)")
    Page<EventEntity> search(String keyword, Set<UUID> orgIds, AppUtils.EventStatus status, Pageable pageable);

    /**
     * Finds an EventEntity by its eventName.
     *
     * @param eventName
     *            - the ID of the event to find
     * @return the EventEntity if found, otherwise null
     */
    boolean existsByEventName(String eventName);

    /**
     * Finds an EventEntity by eventEndDateTimeAfter.
     *
     * @param eventEndDateTimeAfter
     *            - the date and time after which events should be found
     *
     * @return the EventEntity if found, otherwise null
     */
    List<EventEntity> getEventEntitiesByEventEndDateTimeAfterOrderByEventEndDateTime(
            ZonedDateTime eventEndDateTimeAfter);

    /**
     * Finds the top 3 EventEntities that have an eventEndDateTime after the
     * specified dateTime,
     *
     * @param dateTime
     *            - the date and time after which events should be found
     * @return - a list of the top 3 EventEntities ordered by eventEndDateTime in
     *         ascending order
     */
    List<EventEntity> findTop3ByEventEndDateTimeAfterOrderByEventEndDateTimeAsc(ZonedDateTime dateTime);
}
