package com.careloop.repository;

import com.careloop.model.Delivery;
import com.careloop.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByDonationId(Long donationId);
    List<Delivery> findByVolunteerIdOrderByAssignedAtDesc(Long volunteerId);
    List<Delivery> findByStatusInAndVolunteerId(List<DeliveryStatus> statuses, Long volunteerId);
    long countByStatus(DeliveryStatus status);
}
