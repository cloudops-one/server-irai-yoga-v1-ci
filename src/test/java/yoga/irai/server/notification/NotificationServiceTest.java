package yoga.irai.server.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yoga.irai.server.organization.OrganizationService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testSendNotification() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("success");
        notificationService.sendNotificationToTopic("test_topic", "", "", "", "");
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void testSendNotification_withoutTopic() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                notificationService.sendNotificationToTopic("", "", "", "", ""));
        assertTrue(exception.getMessage().contentEquals("Topic must not be null or empty"));
    }

    @Test
    void testSendNotification_NoTopic() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                notificationService.sendNotificationToTopic(null, "", "", "", ""));
        assertTrue(exception.getMessage().contentEquals("Topic must not be null or empty"));
    }

    @Test
    void testSendNotification_NoBody() throws Exception {
        when(firebaseMessaging.send(any(Message.class))).thenReturn("success");
        notificationService.sendNotificationToTopic("test_topic", null, null, "", "");
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void testSubscribeAllTopic() throws FirebaseMessagingException {
        when(organizationService.getTopicName()).thenReturn("topic");
        when(firebaseMessaging.subscribeToTopic(anyList(), anyString())).thenReturn(null);
        notificationService.subscribeAllTopics("token");
        verify(firebaseMessaging,times(12)).subscribeToTopic(anyList(), anyString());
    }

    @Test
    void testUnSubscribeAllTopic() throws FirebaseMessagingException {
        when(organizationService.getTopicName()).thenReturn("topic");
        when(firebaseMessaging.unsubscribeFromTopic(anyList(), anyString())).thenReturn(null);
        notificationService.unsubscribeAllTopics("token");
        verify(firebaseMessaging,times(6)).unsubscribeFromTopic(anyList(), anyString());
    }
}
