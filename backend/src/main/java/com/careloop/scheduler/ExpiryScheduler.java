package com.careloop.scheduler;

import com.careloop.service.DonationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs every 5 minutes to expire old food donations and send expiry warnings.
 */
@Component
public class ExpiryScheduler {

    private final DonationService donationService;

    public ExpiryScheduler(DonationService donationService) {
        this.donationService = donationService;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkExpiry() {
        donationService.processExpiredDonations();
    }
}
