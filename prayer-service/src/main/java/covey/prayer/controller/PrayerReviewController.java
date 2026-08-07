package covey.prayer.controller;

import covey.prayer.model.PrayerRequest;
import covey.prayer.service.PrayerRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/prayers")
public class PrayerReviewController {

    private final PrayerRequestService prayerRequestService;

    public PrayerReviewController(PrayerRequestService prayerRequestService) {
        this.prayerRequestService = prayerRequestService;
    }

    @GetMapping
    public List<PrayerRequest> getAll() {
        return prayerRequestService.getAll();
    }

    @GetMapping("/{id}")
    public PrayerRequest getById(@PathVariable Long id) {
        return prayerRequestService.getById(id);
    }

    @PutMapping("/{id}")
    public PrayerRequest update(@PathVariable Long id, @Valid @RequestBody PrayerRequest request) {
        return prayerRequestService.update(id, request);
    }

    @GetMapping("/pending")
    public List<PrayerRequest> getPending() {
        return prayerRequestService.getAllPending();
    }

    @GetMapping("/rejected")
    public List<PrayerRequest> getRejected() {
        return prayerRequestService.getAllRejected();
    }

    @PostMapping("/{id}/approve")
    public PrayerRequest approve(@PathVariable Long id) {
        return prayerRequestService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long id) {
        prayerRequestService.reject(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        prayerRequestService.delete(id);
    }
}
