package com.careloop.controller;

import com.careloop.dto.*;
import com.careloop.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Donation REST APIs as specified in the requirements.
 */
@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping
    public ResponseEntity<DonationResponse> create(@Valid @RequestBody DonationRequest request) {
        return ResponseEntity.ok(donationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DonationResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(donationService.listAll(type, location));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<DonationResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.verify(id));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<DonationResponse> assign(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.assign(id));
    }

    @PutMapping("/{id}/out-for-delivery")
    public ResponseEntity<DonationResponse> outForDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.outForDelivery(id));
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<DonationResponse> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.deliver(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<DonationResponse> cancel(@PathVariable Long id,
                                                    @Valid @RequestBody CancelRequest request) {
        return ResponseEntity.ok(donationService.cancel(id, request));
    }

    /** Volunteer: my assigned deliveries */
    @GetMapping("/my-deliveries")
    public ResponseEntity<List<DonationResponse>> myDeliveries() {
        return ResponseEntity.ok(donationService.getVolunteerDeliveries());
    }

    /** Public impact stats */
    @GetMapping("/impact")
    public ResponseEntity<Map<String, Object>> impact() {
        return ResponseEntity.ok(donationService.getImpactStats());
    }
}
