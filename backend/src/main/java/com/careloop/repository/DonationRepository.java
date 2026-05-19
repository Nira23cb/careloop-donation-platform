package com.careloop.repository;

import com.careloop.model.Donation;
import com.careloop.model.DonationStatus;
import com.careloop.model.DonationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    List<Donation> findByStatusInOrderByCreatedAtDesc(List<DonationStatus> statuses);
    List<Donation> findByStatusAndType(DonationStatus status, DonationType type);
    List<Donation> findByStatusAndExpiryTimeBefore(DonationStatus status, LocalDateTime time);
    long countByStatus(DonationStatus status);
    List<Donation> findByStatus(DonationStatus status);
}
