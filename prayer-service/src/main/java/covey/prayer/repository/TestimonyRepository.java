package covey.prayer.repository;

import covey.prayer.model.Testimony;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonyRepository extends JpaRepository<Testimony, Long> {
    List<Testimony> findByStatus(Testimony.TestimonyStatus status);
    List<Testimony> findByStatusOrderByCreatedAtDesc(Testimony.TestimonyStatus status);
    List<Testimony> findByPrayerRequestId(Long prayerRequestId);
}
