package yoga.irai.server.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.authentication.service.UserService;
import yoga.irai.server.mobile.dto.EventMobileResponseDto;
import yoga.irai.server.notification.NotificationService;
import yoga.irai.server.organization.OrganizationService;
import yoga.irai.server.storage.StorageService;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private StorageService storageService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventService eventService;

    private UUID eventId;
    private UUID updatedEventIconStorageId;
    private UUID updatedEventBannerStorageId;
    private UUID eventIconStorageId;
    private UUID eventBannerStorageId;
    private UUID orgId;
    private EventEntity eventEntity;
    private EventRequestDto eventRequestDto;
    private String eventIconExternalUrl;
    private String eventBannerExternalUrl;
    private String updateEventIconExternalUrl;
    private String updateEventBannerExternalUrl;
    private Page<EventEntity> mockPage;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        eventIconStorageId = UUID.randomUUID();
        eventBannerStorageId = UUID.randomUUID();
        eventIconExternalUrl = "http://test.test/icon";
        eventBannerExternalUrl = "http://test.test/banner";
        updatedEventIconStorageId = UUID.randomUUID();
        updatedEventBannerStorageId = UUID.randomUUID();
        updateEventIconExternalUrl = "http://test.test/iconUpdated";
        updateEventBannerExternalUrl = "http://test.test/bannerUpdated";
        orgId = UUID.randomUUID();
        eventRequestDto = EventRequestDto.builder().eventName("Test Event").eventDescription("This is a test event.")
                .eventStartDateTime(ZonedDateTime.now()).orgId(orgId)
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build()))
                .eventIconStorageId(eventIconStorageId)
                .eventBannerStorageId(eventBannerStorageId)
                .urls(List.of(UrlDto.builder().id(0).url("")

                        .type(AppUtils.UrlType.REGISTRATION_LINK).build()))
                .build();
        eventEntity = EventEntity.builder().eventId(eventId).orgId(orgId).eventName(eventRequestDto.getEventName())
                .eventDescription(eventRequestDto.getEventDescription())
                .eventStartDateTime(eventRequestDto.getEventStartDateTime())
                .eventIconStorageId(eventRequestDto.getEventIconStorageId())
                .eventBannerStorageId(eventRequestDto.getEventBannerStorageId())
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build()))
                .urls(List.of(UrlDto.builder().id(0).url("http://test.test").type(AppUtils.UrlType.REGISTRATION_LINK)
                        .build()))
                .build();
        eventEntity.setCreatedBy(UUID.randomUUID());
        eventEntity.setUpdatedBy(UUID.randomUUID());
        mockPage = new PageImpl<>(List.of(new EventEntity()));
    }

    @Test
    void testAddEvent_Success() {
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.existsByEventName(eventRequestDto.getEventName()))
                .thenReturn(false);
        EventEntity event = eventService.addEvent(eventRequestDto);
        verify(eventRepository, times(1)).save(any(EventEntity.class));
        verify(eventRepository, times(1)).existsByEventName(anyString());
        assert event != null;
        assert event.getEventId().equals(eventId);
    }

    @Test
    void testAddEvent_Failure() {
        when(eventRepository.existsByEventName(eventRequestDto.getEventName()))
                .thenReturn(true);
        AppException ex = assertThrows(AppException.class, () ->
                eventService.addEvent(eventRequestDto));
        assertEquals(AppUtils.Messages.NAME_EXISTS.getMessage(), ex.getMessage());
        verify(eventRepository, never()).save(any(EventEntity.class));
    }

    @Test
    void testUpdateEvent_Success() {
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));
        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);
        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventId().equals(eventId);
    }

    @Test
    void testUpdateEvent_Success_IconStorageId_NoChange() {
        eventRequestDto.setEventIconStorageId(eventIconStorageId);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventIconStorageId().equals(eventIconStorageId);
    }

    @Test
    void testUpdateEvent_Success_BannerStorageId_NoChange() {
        eventRequestDto.setEventBannerStorageId(eventBannerStorageId);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventBannerStorageId().equals(eventBannerStorageId);
    }

    @Test
    void testUpdateEvent_Success_IconStorageId() {
        eventRequestDto.setEventIconStorageId(updatedEventIconStorageId);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));
        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventIconStorageId().equals(updatedEventIconStorageId);
    }

    @Test
    void testUpdateEvent_Success_BannerStorageId() {
        eventRequestDto.setEventBannerStorageId(updatedEventBannerStorageId);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventBannerStorageId().equals(updatedEventBannerStorageId);
    }

    @Test
    void testUpdateEvent_Success_IconStorageUrl_NoChange() {
        eventRequestDto.setEventIconExternalUrl(updateEventIconExternalUrl);
        eventRequestDto.setEventIconStorageId(null);
        eventEntity.setEventIconExternalUrl(updateEventIconExternalUrl);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventIconExternalUrl().equals(updateEventIconExternalUrl);
    }

    @Test
    void testUpdateEvent_Success_BannerStorageUrl_NoChange() {
        eventRequestDto.setEventBannerExternalUrl(updateEventBannerExternalUrl);
        eventRequestDto.setEventBannerStorageId(null);
        eventEntity.setEventBannerExternalUrl(updateEventBannerExternalUrl);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventBannerExternalUrl().equals(updateEventBannerExternalUrl);
    }

    @Test
    void testUpdateEvent_Success_IconStorageUrl() {
        eventRequestDto.setEventIconExternalUrl(updateEventIconExternalUrl);
        eventRequestDto.setEventIconStorageId(null);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventIconExternalUrl().equals(updateEventIconExternalUrl);
    }

    @Test
    void testUpdateEvent_Success_BannerStorageUrl() {
        eventRequestDto.setEventBannerExternalUrl(updateEventBannerExternalUrl);
        eventRequestDto.setEventBannerStorageId(null);
        when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(eventEntity));

        EventEntity event = eventService.updateEvent(eventId, eventRequestDto);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        assert event != null;
        assert event.getEventBannerExternalUrl().equals(updateEventBannerExternalUrl);
    }

    @Test
    void testGetEvents_MobileUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.MOBILE_USER);
            when(eventRepository.search(anyString(), anySet(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            when(organizationService.getOrgIdsForMobile()).thenReturn(Set.of(orgId));
            Page<EventEntity> events = eventService.getEvents(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            verify(eventRepository, times(1)).search(anyString(), anySet(), any(), any(Pageable.class));
            assert events != null;
            assert !events.getContent().isEmpty();
        }
    }

    @Test
    void testGetEvents_PortalUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.PORTAL_USER);
            when(eventRepository.search(anyString(), anySet(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<EventEntity> events = eventService.getEvents(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            assertNotNull(events);
            assertNotNull(events.getContent());
        }
    }

    @Test
    void testGetEvents_AdminUser() {
        try(MockedStatic<AppUtils> mockedAppUtils = mockStatic(AppUtils.class)) {
            mockedAppUtils.when(AppUtils::getPrincipalUserType).thenReturn(AppUtils.UserType.KEYCLOAK_USER);
            when(eventRepository.search(anyString(), any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);
            Page<EventEntity> events = eventService.getEvents(0, 10, "CreatedAt", Sort.Direction.ASC, "");
            assertNotNull(events);
            assertNotNull(events.getContent());
        }
    }

    @Test
    void testDeleteEvents(){
        eventEntity.setEventBannerStorageId(eventBannerStorageId);
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(eventEntity));
        doNothing().when(eventRepository).delete(any(EventEntity.class));
        doNothing().when(storageService).deleteStorageByIds(anySet());
        eventService.deleteEvent(eventId);
        verify(eventRepository, times(1)).delete(any(EventEntity.class));
    }

    @Test
    void testChangeEventStatus_Active_storageUrl(){
        try {
            when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(eventEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(eventIconExternalUrl);
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            eventService.changeEventStatus(eventId, AppUtils.EventStatus.ACTIVE);
            verify(eventRepository, times(1)).save(any(EventEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testChangeEventStatus_Active_externalUrl(){
        try {
            eventEntity.setEventBannerExternalUrl(eventBannerExternalUrl);
            eventEntity.setEventBannerStorageId(null);
            when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(eventEntity));
            when(organizationService.getTopicName()).thenReturn("topic");
            doNothing().when(notificationService).sendNotificationToTopic(anyString(), anyString(), anyString(), anyString(), anyString());
            when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
            when(storageService.getStorageUrl(any(UUID.class))).thenReturn(eventIconExternalUrl);
            when(userService.getUserNameById(any(UUID.class))).thenReturn("username");
            eventService.changeEventStatus(eventId, AppUtils.EventStatus.ACTIVE);
            verify(eventRepository, times(1)).save(any(EventEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testChangeEventStatus_Inactive(){
        try {
            when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(eventEntity));
            when(eventRepository.save(any(EventEntity.class))).thenReturn(eventEntity);
            eventService.changeEventStatus(eventId, AppUtils.EventStatus.INACTIVE);
            verify(eventRepository, times(1)).save(any(EventEntity.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetTopEvents(){
        when(eventRepository.getEventEntitiesByEventEndDateTimeAfterOrderByEventEndDateTime(any(ZonedDateTime.class))).thenReturn(List.of(eventEntity));
        List<EventEntity> events = eventService.getTopEvents();
        verify(eventRepository, times(1)).getEventEntitiesByEventEndDateTimeAfterOrderByEventEndDateTime(any(ZonedDateTime.class));
        assertNotNull(events);
        assert !events.isEmpty();
    }

    @Test
    void testGetTop3Events(){
        when(eventRepository.findTop3ByEventEndDateTimeAfterOrderByEventEndDateTimeAsc(any(ZonedDateTime.class))).thenReturn(List.of(eventEntity));
        List<EventEntity> events = eventService.getTop3Events();
        verify(eventRepository, times(1)).findTop3ByEventEndDateTimeAfterOrderByEventEndDateTimeAsc(any(ZonedDateTime.class));
        assertNotNull(events);
        assert !events.isEmpty();
    }

    @Test
    void testToEventResponseDto(){
        UUID userId = UUID.randomUUID();
        UUID storageId = UUID.randomUUID();
        UUID orgIconId = UUID.randomUUID();
        when(userService.getUserData(anyList())).thenReturn(Map.of(userId,"username"));
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(storageId,"https://storage.com/url"));
        when(organizationService.getOrgNamesByIds(List.of(orgId))).thenReturn(Map.of(orgIconId,"orgName"));
        when(organizationService.getOrgIconStorageIdToSignedIconUrl(anyList())).thenReturn(Map.of(UUID.randomUUID(),"https://storage.com/url"));
        List<EventResponseDto> eventResponseDtoList = eventService.toEventResponseDto(List.of(eventEntity));
        assertNotNull(eventResponseDtoList);
    }

    @Test
    void testToEventMobileResponseDto(){
        UUID storageId = UUID.randomUUID();
        when(storageService.getSignedStorageUrlByIds(anyList())).thenReturn(Map.of(storageId,"https://storage.com/url"));
        List<EventMobileResponseDto> eventMobileResponseDtoList = eventService.toEventMobileResponseDto(List.of(eventEntity));
        assertNotNull(eventMobileResponseDtoList);
    }
}
