package covey.prayer.controller;

import covey.prayer.model.Testimony;
import covey.prayer.service.TestimonyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class TestimonyController {

    private final TestimonyService testimonyService;

    public TestimonyController(TestimonyService testimonyService) {
        this.testimonyService = testimonyService;
    }

    @GetMapping("/api/public/testimonies")
    public List<Testimony> getApproved() {
        return testimonyService.getAllApproved();
    }

    @GetMapping("/api/public/testimonies/prayer/{prayerRequestId}")
    public List<Testimony> getByPrayer(@PathVariable Long prayerRequestId) {
        return testimonyService.getByPrayerRequest(prayerRequestId);
    }

    @PostMapping("/api/public/testimonies")
    @ResponseStatus(HttpStatus.CREATED)
    public Testimony submit(@Valid @RequestBody Testimony testimony) {
        return testimonyService.create(testimony);
    }

    @GetMapping("/api/admin/testimonies")
    public List<Testimony> getAll() {
        return testimonyService.getAll();
    }

    @GetMapping("/api/admin/testimonies/pending")
    public List<Testimony> getPending() {
        return testimonyService.getAllPending();
    }

    @PostMapping("/api/admin/testimonies/{id}/approve")
    public Testimony approve(@PathVariable Long id) {
        return testimonyService.approve(id);
    }

    @PostMapping("/api/admin/testimonies/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long id) {
        testimonyService.reject(id);
    }

    @DeleteMapping("/api/admin/testimonies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        testimonyService.delete(id);
    }
}
