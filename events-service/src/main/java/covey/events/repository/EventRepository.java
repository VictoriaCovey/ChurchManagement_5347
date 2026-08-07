package covey.events.repository;

import covey.events.model.ChurchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<ChurchEvent, Long> {
    List<ChurchEvent> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);
}
