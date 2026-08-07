package covey.events.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_attendance")
@Getter @Setter @NoArgsConstructor
public class EventAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.REGISTERED;

    private LocalDateTime registeredAt = LocalDateTime.now();

    public enum AttendanceStatus {
        REGISTERED, CANCELLED
    }
}
