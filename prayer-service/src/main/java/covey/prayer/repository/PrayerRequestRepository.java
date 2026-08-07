package covey.prayer.repository;

import covey.prayer.model.PrayerRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, Long> {
    List<PrayerRequest> findByStatus(PrayerRequest.PrayerStatus status);
    List<PrayerRequest> findByStatusOrderByCreatedAtDesc(PrayerRequest.PrayerStatus status);
}
