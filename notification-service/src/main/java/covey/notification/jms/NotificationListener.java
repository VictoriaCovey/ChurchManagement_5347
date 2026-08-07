package covey.notification.jms;

import covey.notification.dto.NotificationMessage;
import covey.notification.model.Notification;
import covey.notification.service.NotificationService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @JmsListener(destination = "${jms.notifications-queue:notifications.queue}")
    public void onNotification(NotificationMessage message) {
        Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setMessage(message.getMessage());
        notification.setType(message.getType());
        notificationService.create(notification);
    }
}
