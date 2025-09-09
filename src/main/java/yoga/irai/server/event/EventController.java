package yoga.irai.server.event;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.storage.StorageService;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/events")
@Tag(name = "Event Management", description = "APIs for managing events, including creation, retrieval, updating, and deletion.")
public class EventController {

    private final EventService eventService;
    private final StorageService storageService;

    /**
     * Adds a new event.
     *
     * @param eventRequestDto
     *            the event data transfer object containing event details
     * @return a response audit containing the ID of the newly added event
     */
    @PostMapping
    @Operation(summary = "Add Event", description = "Create a new event. Returns the ID of the newly added event.")
    public ResponseEntity<AppResponseDto<EventResponseDto>> addEvent(
            @Valid @RequestBody EventRequestDto eventRequestDto) {
        EventEntity eventEntity = eventService.addEvent(eventRequestDto);
        EventResponseDto eventResponseDto = eventService.getEventResponseDto(eventEntity);
        return ResponseEntity.ok(AppResponseDto.<EventResponseDto>builder().data(eventResponseDto)
                .message(AppUtils.Messages.ADD_SUCCESS.getMessage()).build());
    }

    /**
     * Updates an existing event.
     *
     * @param eventId
     *            the ID of the event to update
     * @param eventRequestDto
     *            the updated event data transfer object
     * @return a response audit containing the updated event audit
     */
    @PutMapping("/{eventId}")
    @Operation(summary = "Update Event", description = "Update an existing event by its Id. Returns the updated event audit.")
    public ResponseEntity<AppResponseDto<EventResponseDto>> updateEvent(@PathVariable UUID eventId,
            @Valid @RequestBody EventRequestDto eventRequestDto) {
        EventEntity eventEntity = eventService.updateEvent(eventId, eventRequestDto);
        EventResponseDto eventResponseDto = eventService.getEventResponseDto(eventEntity);
        return ResponseEntity.ok(AppResponseDto.<EventResponseDto>builder().data(eventResponseDto)
                .message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param eventId
     *            the ID of the event to retrieve
     * @return a response audit containing the event audit if found, or an error
     *         message if not found
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "Get Event by Id", description = "Retrieve an event by its Id. Returns the event audit if found, or an error message if not found.")
    public ResponseEntity<AppResponseDto<EventResponseDto>> getEventById(@PathVariable UUID eventId) {
        EventEntity eventEntity = eventService.getEventById(eventId);
        EventResponseDto eventResponseDto = eventService.getEventResponseDto(eventEntity);
        return ResponseEntity.ok(AppResponseDto.<EventResponseDto>builder().data(eventResponseDto)
                .message(AppUtils.Messages.EVENT_FOUND.getMessage(eventId)).build());
    }

    /**
     * Retrieves a paginated list of events, optionally filtered by a search
     * keyword.
     *
     * @param pageNumber
     *            the page number
     * @param pageSize
     *            the page size
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword to filter events by name or description
     * @return a paginated response containing the list of events
     */
    @GetMapping
    @Operation(summary = "Get Events", description = "Get events based on a keyword. Returns a paginated list of events matching the keyword.")
    public ResponseEntity<AppResponseDto<List<EventResponseDto>>> getEvents(
            @RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String keyword) {
        Page<EventEntity> eventPage = eventService.getEvents(pageNumber, pageSize, sortBy, direction, keyword);
        List<EventEntity> eventEntities = eventPage.getContent();
        return ResponseEntity.ok(
                AppResponseDto.<List<EventResponseDto>>builder().data(eventService.toEventResponseDto(eventEntities))
                        .message(AppUtils.Messages.SEARCH_FOUND.getMessage())
                        .pageable(AppResponseDto.buildPageable(eventPage, sortBy, direction)).build());
    }

    /**
     * Deletes an event by its ID.
     *
     * @param eventId
     *            the ID of the event to delete
     * @return a response audit indicating success or failure
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete Event", description = "Delete an event by its ID. Returns a success message if the event is deleted successfully.")
    public ResponseEntity<AppResponseDto<Void>> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(
                AppResponseDto.<Void>builder().message(AppUtils.Messages.DELETE_SUCCESS.getMessage(eventId)).build());
    }

    /**
     * Changes the status of an event by its ID.
     *
     * @param eventId
     *            the ID of the event to change status
     * @param status
     *            the new status to set for the event
     */
    @PutMapping("/status/{eventId}")
    @Operation(summary = "Change Event Status", description = "Change the status of an event by its ID. Returns the updated event audit.")
    public ResponseEntity<AppResponseDto<Void>> changeEventStatus(@PathVariable UUID eventId,
            @RequestParam AppUtils.EventStatus status) {
        eventService.changeEventStatus(eventId, status);
        return ResponseEntity
                .ok(AppResponseDto.<Void>builder().message(AppUtils.Messages.UPDATE_SUCCESS.getMessage()).build());
    }

    /**
     * Retrieves the top events for the portal dashboard.
     *
     * @return a response audit containing a list of top events
     */
    @GetMapping("/dashboard/portal")
    @Operation(summary = "Get Portal Dashboard Events", description = "Retrieve the top events for the portal dashboard. Returns a list of top events.")
    public ResponseEntity<AppResponseDto<List<EventResponseDto>>> getPortalDashboard() {
        return ResponseEntity.ok(AppResponseDto.<List<EventResponseDto>>builder()
                .data(getEventResponseDtos(eventService.getTopEvents())).build());
    }

    /**
     * Retrieves the top events for the portal dashboard.
     *
     * @param topEvents
     *            a list of top event entities
     * @return a list of event response DTOs
     */
    private List<EventResponseDto> getEventResponseDtos(List<EventEntity> topEvents) {
        return topEvents.stream()
                .map(event -> EventResponseDto.builder().eventId(event.getEventId()).eventName(event.getEventName())
                        .eventIconExternalUrl(event.getEventIconExternalUrl())
                        .eventIconStorageUrl(storageService.getStorageUrl(event.getEventIconStorageId()))
                        .eventStartDateTime(event.getEventStartDateTime()).eventEndDateTime(event.getEventEndDateTime())
                        .eventDescription(event.getEventDescription()).addresses(event.getAddresses()).build())
                .toList();
    }
}
