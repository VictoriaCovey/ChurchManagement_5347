package covey.prayer.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "testimonies")
@Getter @Setter @NoArgsConstructor
public class Testimony {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long prayerRequestId;

    private Long memberId;

    private String memberName;

    @NotBlank
    @Size(max = 5000)
    @Column(length = 5000, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestimonyStatus status = TestimonyStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;

    public enum TestimonyStatus {
        PENDING, APPROVED, REJECTED
    }
}
