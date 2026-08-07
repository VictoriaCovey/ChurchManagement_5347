package covey.prayer.service;

import covey.prayer.model.Testimony;
import covey.prayer.repository.TestimonyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestimonyService {

    private final TestimonyRepository testimonyRepository;

    public TestimonyService(TestimonyRepository testimonyRepository) {
        this.testimonyRepository = testimonyRepository;
    }

    public Testimony create(Testimony testimony) {
        testimony.setStatus(Testimony.TestimonyStatus.PENDING);
        return testimonyRepository.save(testimony);
    }

    public Testimony getById(Long id) {
        return testimonyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimony not found"));
    }

    public List<Testimony> getAll() {
        return testimonyRepository.findAll();
    }

    public List<Testimony> getAllApproved() {
        return testimonyRepository.findByStatusOrderByCreatedAtDesc(Testimony.TestimonyStatus.APPROVED);
    }

    public List<Testimony> getAllPending() {
        return testimonyRepository.findByStatus(Testimony.TestimonyStatus.PENDING);
    }

    public List<Testimony> getByPrayerRequest(Long prayerRequestId) {
        return testimonyRepository.findByPrayerRequestId(prayerRequestId);
    }

    public Testimony approve(Long id) {
        Testimony testimony = getById(id);
        testimony.setStatus(Testimony.TestimonyStatus.APPROVED);
        testimony.setReviewedAt(LocalDateTime.now());
        return testimonyRepository.save(testimony);
    }

    public void reject(Long id) {
        Testimony testimony = getById(id);
        testimony.setStatus(Testimony.TestimonyStatus.REJECTED);
        testimony.setReviewedAt(LocalDateTime.now());
        testimonyRepository.save(testimony);
    }

    public void delete(Long id) {
        if (!testimonyRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Testimony not found");
        }
        testimonyRepository.deleteById(id);
    }
}
