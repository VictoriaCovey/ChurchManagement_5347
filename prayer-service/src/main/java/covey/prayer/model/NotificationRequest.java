package covey.prayer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String message;
    private String type;
}
