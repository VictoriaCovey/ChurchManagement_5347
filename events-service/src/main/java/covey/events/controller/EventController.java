package covey.events.controller;

import covey.events.model.ChurchEvent;
import covey.events.model.EventAttendance;
import covey.events.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<ChurchEvent> getUpcoming() {
        return eventService.getUpcomingEvents();
    }

    @GetMapping("/all")
    public List<ChurchEvent> getAll() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ChurchEvent getById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChurchEvent create(@Valid @RequestBody ChurchEvent event) {
        return eventService.createEvent(event);
    }

    @PutMapping("/{id}")
    public ChurchEvent update(@PathVariable Long id, @Valid @RequestBody ChurchEvent event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @PostMapping("/{id}/register")
    @ResponseStatus(HttpStatus.CREATED)
    public EventAttendance register(@PathVariable Long id, @RequestBody EventAttendance attendance) {
        return eventService.register(id, attendance);
    }

    @DeleteMapping("/{id}/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRegistration(@PathVariable Long id, @RequestParam String memberName) {
        eventService.cancelRegistration(id, memberName);
    }

    @GetMapping("/{id}/attendees")
    public List<EventAttendance> getAttendees(@PathVariable Long id) {
        return eventService.getAttendees(id);
    }
}
