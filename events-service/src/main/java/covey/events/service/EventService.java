package covey.events.service;

import covey.events.dto.NotificationMessage;
import covey.events.model.ChurchEvent;
import covey.events.model.EventAttendance;
import covey.events.repository.AttendanceRepository;
import covey.events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final JmsTemplate jmsTemplate;

    @Value("${jms.notifications-queue:notifications.queue}")
    private String notificationsQueue;

    public EventService(EventRepository eventRepository, AttendanceRepository attendanceRepository,
                         JmsTemplate jmsTemplate) {
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.jmsTemplate = jmsTemplate;
    }

    public ChurchEvent createEvent(ChurchEvent event) {
        return eventRepository.save(event);
    }

    public ChurchEvent getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    public List<ChurchEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<ChurchEvent> getUpcomingEvents() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());
    }

    public ChurchEvent updateEvent(Long id, ChurchEvent updated) {
        ChurchEvent existing = getEventById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setEventDate(updated.getEventDate());
        existing.setLocation(updated.getLocation());
        existing.setCapacity(updated.getCapacity());
        return eventRepository.save(existing);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        eventRepository.deleteById(id);
    }

    public EventAttendance register(Long eventId, EventAttendance attendance) {
        ChurchEvent event = getEventById(eventId);

        List<EventAttendance> current = attendanceRepository.findByEventId(eventId).stream()
                .filter(a -> a.getStatus() == EventAttendance.AttendanceStatus.REGISTERED)
                .toList();

        if (event.getCapacity() != null && current.size() >= event.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is at full capacity");
        }

        attendanceRepository.findByEventIdAndMemberName(eventId, attendance.getMemberName())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered for this event");
                });

        attendance.setEventId(eventId);
        attendance.setStatus(EventAttendance.AttendanceStatus.REGISTERED);
        EventAttendance saved = attendanceRepository.save(attendance);

        try {
            jmsTemplate.convertAndSend(notificationsQueue, new NotificationMessage(
                    attendance.getMemberId(),
                    "You're registered for " + event.getTitle(),
                    "EVENT_REGISTRATION"
            ));
        } catch (Exception e) {
            // broker unavailable — continue without failing the registration
        }

        return saved;
    }

    public void cancelRegistration(Long eventId, String memberName) {
        EventAttendance attendance = attendanceRepository.findByEventIdAndMemberName(eventId, memberName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        attendance.setStatus(EventAttendance.AttendanceStatus.CANCELLED);
        attendanceRepository.save(attendance);
    }

    public List<EventAttendance> getAttendees(Long eventId) {
        return attendanceRepository.findByEventId(eventId);
    }
}
