package covey.notification.controller;

import covey.notification.model.Notification;
import covey.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification create(@Valid @RequestBody Notification notification) {
        return notificationService.create(notification);
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable Long userId) {
        return notificationService.getByUser(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnread(@PathVariable Long userId) {
        return notificationService.getUnreadByUser(userId);
    }

    @PatchMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }
}
