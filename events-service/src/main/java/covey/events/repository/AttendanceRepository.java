package covey.events.repository;

import covey.events.model.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<EventAttendance, Long> {
    List<EventAttendance> findByEventId(Long eventId);
    List<EventAttendance> findByMemberId(Long memberId);
    Optional<EventAttendance> findByEventIdAndMemberName(Long eventId, String memberName);
}
