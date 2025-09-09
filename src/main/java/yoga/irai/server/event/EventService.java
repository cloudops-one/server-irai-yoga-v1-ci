package yoga.irai.server.event;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.EventMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.storage.StorageService;

/**
 * This service handles operations related to events in the application.
 */
@Service
@AllArgsConstructor
public class EventService {

    private final UserService userService;
    private final StorageService storageService;
    private final EventRepository eventRepository;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;

    /**
     * Adds a new event.
     *
     * @param eventRequestDto
     *            the event data transfer object containing event details
     * @return the ID of the newly added event
     */
    public EventEntity addEvent(EventRequestDto eventRequestDto) {
        if (eventRepository.existsByEventName(eventRequestDto.getEventName())) {
            throw new AppException(AppUtils.Messages.NAME_EXISTS.getMessage());
        }
        EventEntity eventEntity = AppUtils.map(eventRequestDto, EventEntity.class);
        AppUtils.updateIdsWithPrimary(eventEntity.getAddresses());
        AppUtils.updateIdsWithPrimary(eventEntity.getContacts());
        AppUtils.updateIds(eventEntity.getUrls());
        return eventRepository.save(eventEntity);
    }

    /**
     * Updates an existing event.
     *
     * @param eventId
     *            the ID of the event to update
     * @param eventRequestDto
     *            the updated event data transfer object
     * @return the updated event audit
     */
    @Transactional
    public EventEntity updateEvent(UUID eventId, EventRequestDto eventRequestDto) {
        EventEntity eventEntity = getEventById(eventId);
        if (ObjectUtils.isEmpty(eventEntity.getEventIconExternalUrl())
                && ObjectUtils.isNotEmpty(eventRequestDto.getEventIconStorageId())
                && !eventRequestDto.getEventIconStorageId().equals(eventEntity.getEventIconStorageId())) {
            storageService.deleteStorageById(eventEntity.getEventIconStorageId());
        }
        if (ObjectUtils.isEmpty(eventEntity.getEventBannerExternalUrl())
                && ObjectUtils.isNotEmpty(eventRequestDto.getEventBannerStorageId())
                && !eventRequestDto.getEventBannerStorageId().equals(eventEntity.getEventBannerStorageId())) {
            storageService.deleteStorageById(eventEntity.getEventBannerStorageId());
        }
        AppUtils.map(eventRequestDto, eventEntity);
        eventEntity.setEventId(eventId);
        if (ObjectUtils.isNotEmpty(eventRequestDto.getEventIconStorageId())) {
            eventEntity.setEventIconStorageId(eventRequestDto.getEventIconStorageId());
            eventEntity.setEventIconExternalUrl(null);
        } else {
            eventEntity.setEventIconStorageId(null);
            eventEntity.setEventIconExternalUrl(eventRequestDto.getEventIconExternalUrl());
        }
        if (ObjectUtils.isNotEmpty(eventRequestDto.getEventBannerStorageId())) {
            eventEntity.setEventBannerStorageId(eventRequestDto.getEventBannerStorageId());
            eventEntity.setEventBannerExternalUrl(null);
        } else {
            eventEntity.setEventBannerStorageId(null);
            eventEntity.setEventBannerExternalUrl(eventRequestDto.getEventBannerExternalUrl());
        }
        AppUtils.updateIdsWithPrimary(eventEntity.getAddresses());
        AppUtils.updateIdsWithPrimary(eventEntity.getContacts());
        AppUtils.updateIds(eventEntity.getUrls());
        return eventRepository.save(eventEntity);
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param eventId
     *            the ID of the event to retrieve
     * @return the event audit if found, otherwise null
     */
    public EventEntity getEventById(UUID eventId) {
        return eventRepository.findById(eventId).orElseThrow(AppUtils.Messages.EVENT_NOT_FOUND::getException);
    }

    /**
     * Retrieves a paginated list of events, optionally filtered by a search
     * keyword.
     *
     * @param pageNumber
     *            the pageNumber number
     * @param pageSize
     *            the pageNumber pageSize
     * @param sortBy
     *            the field to sort by
     * @param direction
     *            the sort direction (ASC or DESC)
     * @param keyword
     *            an optional search keyword to filter events by name or description
     * @return a paginated response containing the list of events
     */
    public Page<EventEntity> getEvents(int pageNumber, int pageSize, String sortBy, Sort.Direction direction,
            String keyword) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
        Set<UUID> orgIds = new HashSet<>();
        orgIds.add(AppUtils.getPrincipalOrgId());

        return switch (AppUtils.getPrincipalUserType()) {
            case MOBILE_USER -> eventRepository.search(keyword, organizationService.getOrgIdsForMobile(),
                    AppUtils.EventStatus.INACTIVE, pageable);
            case PORTAL_USER -> eventRepository.search(keyword, orgIds, null, pageable);
            case KEYCLOAK_USER -> eventRepository.search(keyword, null, null, pageable);
        };
    }

    /**
     * Deletes an event by its ID.
     *
     * @param eventId
     *            the ID of the event to delete
     */
    public void deleteEvent(UUID eventId) {
        EventEntity eventEntity = getEventById(eventId);
        Set<UUID> storageIds = Stream.of(eventEntity.getEventIconStorageId(), eventEntity.getEventBannerStorageId())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ObjectUtils.isNotEmpty(storageIds)) {
            storageService.deleteStorageByIds(storageIds);
        }
        eventRepository.delete(getEventById(eventId));
    }

    /**
     * Changes the status of an event.
     *
     * @param eventId
     *            the ID of the event to change status
     * @param status
     *            the new status to set for the event
     */
    public void changeEventStatus(UUID eventId, AppUtils.EventStatus status) {
        EventEntity eventEntity = getEventById(eventId);
        eventEntity.setEventStatus(status);
        eventRepository.save(eventEntity);
        if (AppUtils.EventStatus.ACTIVE.equals(status)) {
            sendEventNotification(getEventResponseDto(eventEntity));
        }
    }

    /**
     * Retrieves a list of upcoming events that have not yet ended.
     *
     * @return a list of upcoming event entities
     */
    public List<EventEntity> getTopEvents() {
        return eventRepository.getEventEntitiesByEventEndDateTimeAfterOrderByEventEndDateTime(ZonedDateTime.now());
    }

    /**
     * Retrieves the top 3 upcoming events that have not yet ended.
     *
     * @return a list of the top 3 upcoming event entities
     */
    public List<EventEntity> getTop3Events() {
        return eventRepository.findTop3ByEventEndDateTimeAfterOrderByEventEndDateTimeAsc(ZonedDateTime.now());
    }

    /**
     * Converts a list of EventEntity to a list of EventResponseDto.
     *
     * @param eventEntities
     *            the list of EventEntity to convert
     * @return a list of EventResponseDto
     */
    public List<EventResponseDto> toEventResponseDto(List<EventEntity> eventEntities) {
        Map<UUID, String> userNamesByIds = userService.getUserData(eventEntities);
        Map<UUID, String> signedStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(
                        eventEntities.stream()
                                .flatMap(eventEntity -> Stream.of(eventEntity.getEventIconStorageId(),
                                        eventEntity.getEventBannerStorageId()))
                                .filter(Objects::nonNull).distinct().toList());
        List<UUID> orgIds = (eventEntities.stream().flatMap(eventEntity -> Stream.of(eventEntity.getOrgId()))
                .filter(Objects::nonNull).distinct().toList());
        Map<UUID, String> orgNamesByIds = organizationService.getOrgNamesByIds(orgIds);
        Map<UUID, String> orgIconStorageUrlByIds = organizationService.getOrgIconStorageIdToSignedIconUrl(orgIds);
        return eventEntities.stream().map(eventEntity -> {
            EventResponseDto eventResponseDto = AppUtils.map(eventEntity, EventResponseDto.class);
            eventResponseDto.setCreatedByName(userNamesByIds.get(eventResponseDto.getCreatedBy()));
            eventResponseDto.setUpdatedByName(userNamesByIds.get(eventResponseDto.getUpdatedBy()));
            eventResponseDto.setOrgName(orgNamesByIds.get(eventEntity.getOrgId()));
            eventResponseDto.setEventIconStorageUrl(signedStorageUrlByIds.get(eventEntity.getEventIconStorageId()));
            eventResponseDto.setEventBannerStorageUrl(signedStorageUrlByIds.get(eventEntity.getEventBannerStorageId()));
            eventResponseDto.setOrgIconStorageUrl(orgIconStorageUrlByIds.get(eventEntity.getOrgId()));
            return eventResponseDto;
        }).toList();
    }

    /**
     * Converts a list of EventEntity to a list of EventMobileResponseDto.
     *
     * @param eventEntities
     *            the list of EventEntity to convert
     * @return a list of EventMobileResponseDto
     */
    public List<EventMobileResponseDto> toEventMobileResponseDto(List<EventEntity> eventEntities) {
        Map<UUID, String> signedStorageUrlByIds = storageService
                .getSignedStorageUrlByIds(
                        eventEntities.stream()
                                .flatMap(eventEntity -> Stream.of(eventEntity.getEventIconStorageId(),
                                        eventEntity.getEventBannerStorageId()))
                                .filter(Objects::nonNull).distinct().toList());
        return eventEntities.stream().map(eventEntity -> {
            EventMobileResponseDto eventMobileResponseDto = AppUtils.map(eventEntity, EventMobileResponseDto.class);
            eventMobileResponseDto
                    .setEventIconStorageUrl(signedStorageUrlByIds.get(eventEntity.getEventIconStorageId()));
            eventMobileResponseDto
                    .setEventBannerStorageUrl(signedStorageUrlByIds.get(eventEntity.getEventBannerStorageId()));
            return eventMobileResponseDto;
        }).toList();
    }

    /**
     * Sends a notification for the given Event.
     *
     * @param eventResponseDto
     *            the DTO containing Event details
     */
    private void sendEventNotification(EventResponseDto eventResponseDto) {
        try {
            String title = eventResponseDto.getEventName();
            String body = eventResponseDto.getEventDescription();
            String imageUrl = "";
            if (!ObjectUtils.isEmpty(eventResponseDto.getEventBannerStorageUrl())) {
                imageUrl = eventResponseDto.getEventBannerStorageUrl();
            } else if (!ObjectUtils.isEmpty(eventResponseDto.getEventBannerExternalUrl())) {
                imageUrl = eventResponseDto.getEventBannerExternalUrl();
            }
            notificationService.sendNotificationToTopic(
                    organizationService.getTopicName() + "_" + AppUtils.ModuleType.EVENT, title, body, imageUrl,
                    eventResponseDto.getEventId().toString());
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * @param eventEntity
     *            contains all events data.
     *
     * @return a response audit containing all data from events
     */
    public EventResponseDto getEventResponseDto(EventEntity eventEntity) {
        EventResponseDto eventResponseDto = AppUtils.map(eventEntity, EventResponseDto.class);
        if (ObjectUtils.isNotEmpty(eventEntity.getEventIconStorageId())) {
            eventResponseDto.setEventIconStorageUrl(storageService.getStorageUrl(eventEntity.getEventIconStorageId()));
        }
        if (ObjectUtils.isNotEmpty(eventEntity.getEventBannerStorageId())) {
            eventResponseDto
                    .setEventBannerStorageUrl(storageService.getStorageUrl(eventEntity.getEventBannerStorageId()));
        }
        eventResponseDto.setOrgName(organizationService.getOrgNameByOrgId(eventEntity.getOrgId()));
        eventResponseDto
                .setOrgIconStorageUrl(organizationService.getOrgIconStorageIdToSignedIconUrl(eventEntity.getOrgId()));
        eventResponseDto.setCreatedByName(userService.getUserNameById(eventEntity.getCreatedBy()));
        eventResponseDto.setUpdatedByName(userService.getUserNameById(eventEntity.getUpdatedBy()));
        return eventResponseDto;
    }
}
