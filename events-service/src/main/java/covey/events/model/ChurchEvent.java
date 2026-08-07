package covey.events.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "church_events")
@Getter @Setter @NoArgsConstructor
public class ChurchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Size(max = 200)
    private String location;

    private Integer capacity;

    private LocalDateTime createdAt = LocalDateTime.now();
}
