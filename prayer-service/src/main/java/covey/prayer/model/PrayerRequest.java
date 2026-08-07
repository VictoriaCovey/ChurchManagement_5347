package covey.prayer.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "prayer_requests")
@Getter @Setter @NoArgsConstructor
public class PrayerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String requesterName;

    @NotBlank
    @Size(max = 5000)
    @Column(length = 5000, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrayerStatus status = PrayerStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;

    public enum PrayerStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
