package covey.notification.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @NotBlank
    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
