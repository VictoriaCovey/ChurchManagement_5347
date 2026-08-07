package covey.prayer.controller;

import covey.prayer.model.PrayerRequest;
import covey.prayer.service.PrayerRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/public/prayers")
public class PrayerRequestController {

    private final PrayerRequestService prayerRequestService;

    public PrayerRequestController(PrayerRequestService prayerRequestService) {
        this.prayerRequestService = prayerRequestService;
    }

    @GetMapping
    public List<PrayerRequest> getAllApproved() {
        return prayerRequestService.getAllApproved();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrayerRequest submit(@Valid @RequestBody PrayerRequest request) {
        return prayerRequestService.create(request);
    }
}
