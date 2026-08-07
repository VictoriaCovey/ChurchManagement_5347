package covey.prayer.service;

import covey.prayer.model.NotificationRequest;
import covey.prayer.model.PrayerRequest;
import covey.prayer.model.UserDto;
import covey.prayer.repository.PrayerRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrayerRequestService {

    private final PrayerRequestRepository prayerRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;
    private final JmsTemplate jmsTemplate;

    // Resolved via the Eureka service registry (see AppConfig's @LoadBalanced RestTemplate)
    // rather than a hardcoded host:port.
    @Value("${user.service.url:http://user-service}")
    private String userServiceUrl;

    @Value("${jms.notifications-queue:notifications.queue}")
    private String notificationsQueue;


    public PrayerRequestService(PrayerRequestRepository prayerRequestRepository,
                                SimpMessagingTemplate messagingTemplate,
                                RestTemplate restTemplate,
                                JmsTemplate jmsTemplate) {
        this.prayerRequestRepository = prayerRequestRepository;
        this.messagingTemplate = messagingTemplate;
        this.restTemplate = restTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    public PrayerRequest create(PrayerRequest request) {
        request.setStatus(PrayerRequest.PrayerStatus.PENDING);

        // If a name was provided, try to link it to a registered user in user-service
        if (request.getRequesterName() != null && !request.getRequesterName().isBlank()) {
            try {
                UserDto user = restTemplate.getForObject(
                        userServiceUrl + "/api/users/username/" + request.getRequesterName(),
                        UserDto.class
                );
                if (user != null) {
                    request.setUserId(user.getId());
                    request.setRequesterName(user.getUsername());
                }
            } catch (Exception e) {
                // user-service unavailable or username not found — submit without linking
            }
        }

        PrayerRequest saved = prayerRequestRepository.save(request);
        messagingTemplate.convertAndSend("/topic/admin/pending", saved);
        return saved;
    }

    public PrayerRequest getById(Long id) {
        return prayerRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prayer request not found"));
    }

    public List<PrayerRequest> getAll() {
        return prayerRequestRepository.findAll();
    }

    public List<PrayerRequest> getAllApproved() {
        return prayerRequestRepository.findByStatusOrderByCreatedAtDesc(PrayerRequest.PrayerStatus.APPROVED);
    }

    public List<PrayerRequest> getAllPending() {
        return prayerRequestRepository.findByStatus(PrayerRequest.PrayerStatus.PENDING);
    }

    public List<PrayerRequest> getAllRejected() {
        return prayerRequestRepository.findByStatus(PrayerRequest.PrayerStatus.REJECTED);
    }

    public PrayerRequest update(Long id, PrayerRequest updated) {
        PrayerRequest existing = getById(id);
        existing.setRequesterName(updated.getRequesterName());
        existing.setContent(updated.getContent());
        return prayerRequestRepository.save(existing);
    }

    public PrayerRequest approve(Long id) {
        PrayerRequest request = getById(id);
        request.setStatus(PrayerRequest.PrayerStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        prayerRequestRepository.save(request);
        messagingTemplate.convertAndSend("/topic/prayers", request);
        broadcastApproved();
        sendNotification(request.getUserId(), "Your prayer request has been approved.", "PRAYER_APPROVED");
        return request;
    }

    public void reject(Long id) {
        PrayerRequest request = getById(id);
        request.setStatus(PrayerRequest.PrayerStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        prayerRequestRepository.save(request);
        messagingTemplate.convertAndSend("/topic/admin/remove", id);
        broadcastApproved();
        sendNotification(request.getUserId(), "Your prayer request was not approved.", "PRAYER_REJECTED");
    }

    private void sendNotification(Long userId, String message, String type) {
        if (userId == null) return;
        try {
            jmsTemplate.convertAndSend(notificationsQueue, new NotificationRequest(userId, message, type));
        } catch (Exception e) {
            // broker unavailable — continue without failing the prayer-request workflow
        }
    }

    public void delete(Long id) {
        if (!prayerRequestRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Prayer request not found");
        }
        prayerRequestRepository.deleteById(id);
    }

    public void broadcastApproved() {
        List<PrayerRequest> recent = getAllApproved().stream().limit(50).toList();
        messagingTemplate.convertAndSend("/topic/recentPrayers", recent);
    }
}
