package com.careloop.service;

import com.careloop.dto.*;
import com.careloop.model.*;
import com.careloop.repository.*;
import com.careloop.security.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic for donations, deliveries, trust scores, and food expiry.
 */
@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${careloop.food.expiry-hours:4}")
    private int foodExpiryHours;

    private static final int URGENT_MINUTES = 60;
    private static final int MAX_CANCELLATIONS = 3;

    public DonationService(DonationRepository donationRepository,
                           DeliveryRepository deliveryRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.donationRepository = donationRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /** Donor creates a new donation */
    @Transactional
    public DonationResponse create(DonationRequest req) {
        Long donorId = requireUser();
        requireRole(Role.DONOR);

        User donor = userRepository.findById(donorId).orElseThrow();
        if (Boolean.TRUE.equals(donor.getUnreliable())) {
            throw new RuntimeException("Your account is marked unreliable due to frequent cancellations");
        }

        Donation d = new Donation();
        d.setDonorId(donorId);
        d.setType(req.getType());
        d.setQuantity(req.getQuantity());
        d.setLocation(req.getLocation());
        d.setVegType(req.getVegType() != null ? req.getVegType() : VegType.NA);
        d.setStatus(DonationStatus.PENDING);

        if (req.getType() == DonationType.FOOD && req.getPreparedTime() != null && !req.getPreparedTime().isBlank()) {
            LocalDateTime prepared = LocalDateTime.parse(req.getPreparedTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            d.setPreparedTime(prepared);
            d.setExpiryTime(prepared.plusHours(foodExpiryHours));
        }

        donationRepository.save(d);
        return toResponse(d);
    }

    /** List donations based on role */
    public List<DonationResponse> listAll(String typeFilter, String locationFilter) {
        Long userId = requireUser();
        Role role = UserContext.getRole();
        List<Donation> donations;

        if (role == Role.DONOR) {
            donations = donationRepository.findByDonorIdOrderByCreatedAtDesc(userId);
        } else if (role == Role.NGO) {
            donations = donationRepository.findByStatusInOrderByCreatedAtDesc(
                    Arrays.asList(DonationStatus.PENDING, DonationStatus.VERIFIED,
                            DonationStatus.ASSIGNED, DonationStatus.OUT_FOR_DELIVERY));
        } else {
            donations = donationRepository.findByStatusInOrderByCreatedAtDesc(
                    Arrays.asList(DonationStatus.VERIFIED, DonationStatus.ASSIGNED,
                            DonationStatus.OUT_FOR_DELIVERY));
        }

        return donations.stream()
                .map(this::toResponse)
                .filter(r -> typeFilter == null || typeFilter.isBlank() || r.getType().name().equalsIgnoreCase(typeFilter))
                .filter(r -> locationFilter == null || locationFilter.isBlank()
                        || r.getLocation().toLowerCase().contains(locationFilter.toLowerCase()))
                .collect(Collectors.toList());
    }

    /** NGO verifies a donation (anti-fake donation) */
    @Transactional
    public DonationResponse verify(Long id) {
        requireRole(Role.NGO);
        Long ngoId = requireUser();

        Donation d = getDonation(id);
        if (d.getStatus() != DonationStatus.PENDING) {
            throw new RuntimeException("Only pending donations can be verified");
        }
        if (isExpired(d)) {
            markExpired(d);
            throw new RuntimeException("Donation has already expired");
        }

        d.setStatus(DonationStatus.VERIFIED);
        d.setVerifiedBy(ngoId);
        d.setVerifiedAt(LocalDateTime.now());
        donationRepository.save(d);

        notificationService.notify(d.getDonorId(),
                "Your " + d.getType().name().toLowerCase() + " donation has been verified!", "VERIFIED");

        return toResponse(d);
    }

    /** Volunteer accepts delivery task */
    @Transactional
    public DonationResponse assign(Long id) {
        requireRole(Role.VOLUNTEER);
        Long volunteerId = requireUser();

        User volunteer = userRepository.findById(volunteerId).orElseThrow();
        if (Boolean.TRUE.equals(volunteer.getUnreliable())) {
            throw new RuntimeException("Your account is marked unreliable. Cannot accept deliveries.");
        }

        Donation d = getDonation(id);
        if (d.getStatus() != DonationStatus.VERIFIED) {
            throw new RuntimeException("Only verified donations can be assigned");
        }
        if (isExpired(d)) {
            markExpired(d);
            throw new RuntimeException("Donation has expired");
        }
        if (deliveryRepository.findByDonationId(id).isPresent()) {
            throw new RuntimeException("Donation already assigned to a volunteer");
        }

        d.setStatus(DonationStatus.ASSIGNED);
        donationRepository.save(d);

        Delivery delivery = new Delivery();
        delivery.setDonationId(id);
        delivery.setVolunteerId(volunteerId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        deliveryRepository.save(delivery);

        notificationService.notify(d.getDonorId(),
                "A volunteer has been assigned to deliver your donation.", "ASSIGNED");

        return toResponse(d);
    }

    /** Volunteer marks out for delivery */
    @Transactional
    public DonationResponse outForDelivery(Long id) {
        requireRole(Role.VOLUNTEER);
        Long volunteerId = requireUser();

        Donation d = getDonation(id);
        Delivery delivery = deliveryRepository.findByDonationId(id)
                .orElseThrow(() -> new RuntimeException("No delivery found"));

        if (!delivery.getVolunteerId().equals(volunteerId)) {
            throw new RuntimeException("This delivery is not assigned to you");
        }
        if (d.getStatus() != DonationStatus.ASSIGNED) {
            throw new RuntimeException("Invalid status transition");
        }

        d.setStatus(DonationStatus.OUT_FOR_DELIVERY);
        delivery.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        donationRepository.save(d);
        deliveryRepository.save(delivery);

        notificationService.notify(d.getDonorId(),
                "Your donation is out for delivery!", "OUT_FOR_DELIVERY");

        return toResponse(d);
    }

    /** Volunteer marks delivered */
    @Transactional
    public DonationResponse deliver(Long id) {
        requireRole(Role.VOLUNTEER);
        Long volunteerId = requireUser();

        Donation d = getDonation(id);
        Delivery delivery = deliveryRepository.findByDonationId(id)
                .orElseThrow(() -> new RuntimeException("No delivery found"));

        if (!delivery.getVolunteerId().equals(volunteerId)) {
            throw new RuntimeException("This delivery is not assigned to you");
        }

        d.setStatus(DonationStatus.DELIVERED);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        donationRepository.save(d);
        deliveryRepository.save(delivery);

        notificationService.notify(d.getDonorId(),
                "Your donation has been delivered successfully! Thank you for giving.", "DELIVERED");

        return toResponse(d);
    }

    /** Cancel with reason — tracks reliability */
    @Transactional
    public DonationResponse cancel(Long id, CancelRequest req) {
        Long userId = requireUser();
        Donation d = getDonation(id);

        boolean canCancel = d.getDonorId().equals(userId)
                || UserContext.getRole() == Role.NGO
                || (UserContext.getRole() == Role.VOLUNTEER
                && deliveryRepository.findByDonationId(id)
                .map(del -> del.getVolunteerId().equals(userId))
                .orElse(false));

        if (!canCancel) {
            throw new RuntimeException("You are not allowed to cancel this donation");
        }
        if (d.getStatus() == DonationStatus.DELIVERED || d.getStatus() == DonationStatus.EXPIRED) {
            throw new RuntimeException("Cannot cancel this donation");
        }

        d.setStatus(DonationStatus.CANCELLED);
        d.setCancelReason(req.getReason());
        donationRepository.save(d);

        deliveryRepository.findByDonationId(id).ifPresent(del -> {
            del.setStatus(DeliveryStatus.CANCELLED);
            deliveryRepository.save(del);
            applyCancellationPenalty(del.getVolunteerId());
        });

        if (d.getDonorId().equals(userId)) {
            applyCancellationPenalty(userId);
        }

        return toResponse(d);
    }

    /** Scheduled: mark expired food donations */
    @Transactional
    public void processExpiredDonations() {
        List<Donation> expiring = donationRepository.findByStatusAndExpiryTimeBefore(
                DonationStatus.PENDING, LocalDateTime.now());
        expiring.addAll(donationRepository.findByStatusAndExpiryTimeBefore(
                DonationStatus.VERIFIED, LocalDateTime.now()));
        expiring.addAll(donationRepository.findByStatusAndExpiryTimeBefore(
                DonationStatus.ASSIGNED, LocalDateTime.now()));

        for (Donation d : expiring) {
            if (d.getExpiryTime() != null) {
                markExpired(d);
            }
        }

        // Warn donors when food expiring within 1 hour
        List<Donation> activeFood = donationRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList(DonationStatus.PENDING, DonationStatus.VERIFIED, DonationStatus.ASSIGNED));
        for (Donation d : activeFood) {
            if (d.getType() == DonationType.FOOD && d.getExpiryTime() != null) {
                long minutes = Duration.between(LocalDateTime.now(), d.getExpiryTime()).toMinutes();
                if (minutes > 0 && minutes <= URGENT_MINUTES) {
                    notificationService.notify(d.getDonorId(),
                            "Food donation expiring in " + minutes + " minutes! Please ensure quick pickup.",
                            "EXPIRING_SOON");
                }
            }
        }
    }

    public Map<String, Object> getImpactStats() {
        long delivered = donationRepository.countByStatus(DonationStatus.DELIVERED);
        long meals = donationRepository.findByStatus(DonationStatus.DELIVERED).stream()
                .filter(d -> d.getType() == DonationType.FOOD)
                .mapToInt(Donation::getQuantity)
                .sum();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDelivered", delivered);
        stats.put("mealsServed", meals);
        return stats;
    }

    public List<DonationResponse> getVolunteerDeliveries() {
        requireRole(Role.VOLUNTEER);
        Long volunteerId = requireUser();
        List<Delivery> deliveries = deliveryRepository.findByVolunteerIdOrderByAssignedAtDesc(volunteerId);
        return deliveries.stream()
                .map(del -> toResponse(getDonation(del.getDonationId())))
                .collect(Collectors.toList());
    }

    private void applyCancellationPenalty(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setCancelCount(user.getCancelCount() + 1);
        int newScore = Math.max(0, user.getReliabilityScore() - 15);
        user.setReliabilityScore(newScore);
        if (user.getCancelCount() > MAX_CANCELLATIONS) {
            user.setUnreliable(true);
        }
        userRepository.save(user);
    }

    private void markExpired(Donation d) {
        d.setStatus(DonationStatus.EXPIRED);
        donationRepository.save(d);
        notificationService.notify(d.getDonorId(),
                "Your food donation has expired due to safety rules (4 hour limit).", "EXPIRED");
    }

    private boolean isExpired(Donation d) {
        return d.getExpiryTime() != null && LocalDateTime.now().isAfter(d.getExpiryTime());
    }

    private Donation getDonation(Long id) {
        return donationRepository.findById(id).orElseThrow(() -> new RuntimeException("Donation not found"));
    }

    private DonationResponse toResponse(Donation d) {
        DonationResponse r = new DonationResponse();
        r.setId(d.getId());
        r.setDonorId(d.getDonorId());
        r.setType(d.getType());
        r.setQuantity(d.getQuantity());
        r.setLocation(d.getLocation());
        r.setPreparedTime(d.getPreparedTime());
        r.setExpiryTime(d.getExpiryTime());
        r.setVegType(d.getVegType());
        r.setStatus(d.getStatus());
        r.setVerified(d.getStatus() != DonationStatus.PENDING && d.getVerifiedBy() != null);
        r.setCreatedAt(d.getCreatedAt());

        userRepository.findById(d.getDonorId()).ifPresent(u -> r.setDonorName(u.getName()));

        deliveryRepository.findByDonationId(d.getId()).ifPresent(del -> {
            r.setVolunteerId(del.getVolunteerId());
            userRepository.findById(del.getVolunteerId()).ifPresent(u -> r.setVolunteerName(u.getName()));
        });

        if (d.getExpiryTime() != null && d.getStatus() != DonationStatus.DELIVERED
                && d.getStatus() != DonationStatus.EXPIRED && d.getStatus() != DonationStatus.CANCELLED) {
            long minutes = Duration.between(LocalDateTime.now(), d.getExpiryTime()).toMinutes();
            r.setMinutesLeft(minutes);
            if (minutes <= 0) {
                r.setTimeLeft("Expired");
                r.setUrgent(true);
            } else if (minutes < 60) {
                r.setTimeLeft(minutes + " min left");
                r.setUrgent(true);
            } else {
                long hours = minutes / 60;
                r.setTimeLeft(hours + "h " + (minutes % 60) + "m left");
                r.setUrgent(minutes <= URGENT_MINUTES);
            }
        } else if (d.getType() == DonationType.FOOD && d.getExpiryTime() == null) {
            r.setTimeLeft("N/A");
        }

        return r;
    }

    private Long requireUser() {
        Long id = UserContext.getUserId();
        if (id == null) throw new RuntimeException("Please login first");
        return id;
    }

    private void requireRole(Role required) {
        if (UserContext.getRole() != required) {
            throw new RuntimeException("Access denied. Required role: " + required);
        }
    }
}
