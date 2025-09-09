package yoga.irai.server.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Collections;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.app.exception.AppException;
import yoga.irai.server.organization.OrganizationService;

@Service
@AllArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final OrganizationService organizationService;
    private final NotificationRepository notificationRepository;

    /**
     * Sends a notification to a specific topic.
     *
     * @param topic
     *            the topic to send the notification to
     * @param title
     *            the title of the notification
     * @param body
     *            the body of the notification
     * @param imageUrl
     *            the image URL of the notification
     * @param id
     *            the id associated with the notification
     * @throws FirebaseMessagingException
     *             if an error occurs while sending the notification
     */
    public void sendNotificationToTopic(String topic, String title, String body, String imageUrl, String id)
            throws FirebaseMessagingException {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic must not be null or empty");
        }

        NotificationEntity notificationEntity = NotificationEntity.builder().title(title == null ? "" : title)
                .message(body == null ? "" : body).topic(topic).imageUrl(imageUrl).url(topic.toLowerCase())
                .referenceId(id).notificationStatus(AppUtils.NotificationStatus.DONE).build();
        notificationRepository.save(notificationEntity);

        Message message = Message.builder().setTopic(topic)
                .setNotification(Notification.builder().setTitle(title == null ? "" : title)
                        .setBody(body == null ? "" : body).setImage(imageUrl).build())
                .putData("url", "/" + topic.toLowerCase().split("_")[1]).putData("id", id).build();
        firebaseMessaging.send(message);
    }

    /**
     * Subscribes a device token to a specific topic.
     *
     * @param topic
     *            the topic to subscribe to
     * @param token
     *            the device token to subscribe
     */
    public void subscribe(String topic, String token) {
        try {
            firebaseMessaging.subscribeToTopic(Collections.singletonList(token), topic);
        } catch (FirebaseMessagingException e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * Unsubscribes a device token from a specific topic.
     *
     * @param topic
     *            the topic to unsubscribe from
     * @param token
     *            the device token to unsubscribe
     */
    public void unsubscribe(String topic, String token) {
        try {
            firebaseMessaging.unsubscribeFromTopic(Collections.singletonList(token), topic);
        } catch (FirebaseMessagingException e) {
            throw new AppException(e.getMessage());
        }
    }

    /**
     * Subscribes a device token to all notification topics.
     *
     * @param token
     *            the device token to subscribe
     */
    public void subscribeAllTopics(String token) {
        for (AppUtils.NotificationTopic topic : AppUtils.NotificationTopic.values()) {
            subscribe(organizationService.getTopicName() + "_" + topic.name(), token);
            subscribe(AppUtils.Constants.DEFAULT_NOTIFICATION_TOPIC + topic.name(), token);
        }
    }

    /**
     * Unsubscribes a device token from all notification topics.
     *
     * @param token
     *            the device token to unsubscribe
     */
    public void unsubscribeAllTopics(String token) {
        for (AppUtils.NotificationTopic topic : AppUtils.NotificationTopic.values()) {
            unsubscribe(organizationService.getTopicName() + "_" + topic.name(), token);
            subscribe(AppUtils.Constants.DEFAULT_NOTIFICATION_TOPIC + topic.name(), token);
        }
    }
}
