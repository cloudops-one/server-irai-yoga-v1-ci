package yoga.irai.server.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.AppUtils.Messages;
import yoga.irai.server.app.dto.AppResponseDto;
import yoga.irai.server.app.dto.ContactDto;
import yoga.irai.server.app.dto.UrlDto;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.storage.StorageService;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private EventController eventController;

    private EventRequestDto eventRequestDto;
    private EventEntity eventEntity;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        eventRequestDto = EventRequestDto.builder().eventName("Test Event").eventDescription("This is a test event.")
                .eventStartDateTime(ZonedDateTime.now()).orgId(orgId)
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build()))
                .eventIconStorageId(UUID.randomUUID()).urls(List.of(UrlDto.builder().id(0).url("http://test.test")
                        .type(AppUtils.UrlType.REGISTRATION_LINK).build()))
                .build();
        eventEntity = EventEntity.builder().eventId(eventId).orgId(orgId).eventName(eventRequestDto.getEventName())
                .eventDescription(eventRequestDto.getEventDescription())
                .eventStartDateTime(eventRequestDto.getEventStartDateTime())
                .eventIconStorageId(eventRequestDto.getEventIconStorageId())
                .eventBannerStorageId(eventRequestDto.getEventBannerStorageId())
                .eventBannerExternalUrl("http://test.test/banner").eventIconExternalUrl("http://test.test/icon")
                .contacts(List.of(ContactDto.builder().id(0).email("test@test.test").name("test")
                        .mobile("+911234567890").build()))
                .urls(List.of(UrlDto.builder().id(0).url("http://test.test").type(AppUtils.UrlType.REGISTRATION_LINK)
                        .build()))
                .build();
    }

    @Test
    void testAddEvent() {
        when(eventService.addEvent(any(EventRequestDto.class))).thenReturn(eventEntity);
        when(eventService.getEventResponseDto(any(EventEntity.class)))
                .thenReturn(EventResponseDto.builder().eventId(eventId).eventName(eventEntity.getEventName())
                        .eventDescription(eventEntity.getEventDescription())
                        .eventStartDateTime(eventEntity.getEventStartDateTime()).orgId(eventEntity.getOrgId())
                        .contacts(eventEntity.getContacts()).urls(eventEntity.getUrls()).build());
        ResponseEntity<AppResponseDto<EventResponseDto>> response = eventController.addEvent(eventRequestDto);

        verify(eventService, times(1)).addEvent(any(EventRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testUpdateEvent() {
        when(eventService.updateEvent(any(UUID.class), any(EventRequestDto.class))).thenReturn(new EventEntity());
        when(eventService.getEventResponseDto(any(EventEntity.class))).thenReturn(EventResponseDto.builder()
                .eventId(eventId).eventName(eventEntity.getEventName())
                .eventDescription(eventEntity.getEventDescription())
                .eventStartDateTime(eventEntity.getEventStartDateTime()).orgId(eventEntity.getOrgId())
                .contacts(eventEntity.getContacts()).urls(eventEntity.getUrls())
                .eventEndDateTime(eventEntity.getEventEndDateTime())
                .build());
        ResponseEntity<AppResponseDto<EventResponseDto>> response = eventController.updateEvent(eventId, eventRequestDto);

        verify(eventService, times(1)).updateEvent(any(UUID.class), any(EventRequestDto.class));
        assert response.getStatusCode() == HttpStatus.OK;
    }

    @Test
    void testGetEventById() {
        when(eventService.getEventById(any(UUID.class))).thenReturn(eventEntity);
        when(eventService.getEventResponseDto(any(EventEntity.class)))
                .thenReturn(EventResponseDto.builder().eventId(eventId).eventName(eventEntity.getEventName())
                        .eventDescription(eventEntity.getEventDescription())
                        .eventStartDateTime(eventEntity.getEventStartDateTime()).orgId(eventEntity.getOrgId())
                        .contacts(eventEntity.getContacts()).urls(eventEntity.getUrls()).build());
        ResponseEntity<AppResponseDto<EventResponseDto>> response = eventController.getEventById(eventId);
        verify(eventService, times(1)).getEventById(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().getEventId().equals(eventId);
    }

    @Test
    void testSearchEvents() {
        Page<EventEntity> eventPage = new PageImpl<>(List.of(eventEntity));
        when(eventService.getEvents(anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(eventPage);
        when(eventService.toEventResponseDto(anyList()))
                .thenReturn(List.of(EventResponseDto.builder().eventId(eventEntity.getEventId())
                        .eventName(eventEntity.getEventName()).eventDescription(eventEntity.getEventDescription())
                        .eventStartDateTime(eventEntity.getEventStartDateTime()).orgId(eventEntity.getOrgId())
                        .contacts(eventEntity.getContacts()).urls(eventEntity.getUrls()).build()));
        ResponseEntity<AppResponseDto<List<EventResponseDto>>> response = eventController.getEvents(0, 0, "",
                Sort.Direction.ASC, "");

        verify(eventService, times(1)).getEvents(anyInt(), anyInt(), anyString(), any(), anyString());
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getData().size() == 1;
    }

    @Test
    void testDeleteEvent() {
        doNothing().when(eventService).deleteEvent(any(UUID.class));

        ResponseEntity<AppResponseDto<Void>> response = eventController.deleteEvent(eventId);

        verify(eventService, times(1)).deleteEvent(any(UUID.class));
        assert response.getStatusCode() == HttpStatus.OK;
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(Messages.DELETE_SUCCESS.getMessage());
    }

    @Test
    void testDeleteEventNotFound() {
        doThrow(new AppException(Messages.EVENT_NOT_FOUND.getMessage(eventId))).when(eventService)
                .deleteEvent(any(UUID.class));

        AppException exception = assertThrows(AppException.class, () -> eventController.deleteEvent(eventId));

        verify(eventService, times(1)).deleteEvent(any(UUID.class));
        assert exception.getMessage().equals(Messages.EVENT_NOT_FOUND.getMessage(eventId));
    }

    @Test
    void testChangeEventStatus() {
        doNothing().when(eventService).changeEventStatus(any(UUID.class), any(AppUtils.EventStatus.class));
        ResponseEntity<AppResponseDto<Void>> response = eventController.changeEventStatus(eventId,
                AppUtils.EventStatus.ACTIVE);
        verify(eventService, times(1)).changeEventStatus(eventId, AppUtils.EventStatus.ACTIVE);
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
        assert response.getBody().getMessage().equals(AppUtils.Messages.UPDATE_SUCCESS.getMessage());
    }

    @Test
    void testGetPortalDashboard() {
        when(storageService.getStorageUrl(any(UUID.class))).thenReturn("url");
        when(eventService.getTopEvents()).thenReturn(List.of(eventEntity));
        ResponseEntity<AppResponseDto<List<EventResponseDto>>> response = eventController.getPortalDashboard();
        verify(eventService, times(1)).getTopEvents();
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody() != null;
    }
}
