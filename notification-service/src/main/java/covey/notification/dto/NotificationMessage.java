package covey.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wire contract for JMS messages arriving on the notifications queue. Mirrors the shape of
 * prayer-service's NotificationRequest / events-service's NotificationMessage - matched by JSON
 * field name, not by sharing a Java class across services.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationMessage {
    private Long userId;
    private String message;
    private String type;
}
