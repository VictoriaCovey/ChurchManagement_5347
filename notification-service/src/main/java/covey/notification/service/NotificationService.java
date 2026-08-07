package covey.notification.service;

import covey.notification.model.Notification;
import covey.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    public List<Notification> getByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndRead(userId, false);
    }

    public Notification markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
