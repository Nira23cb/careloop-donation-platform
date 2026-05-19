package com.careloop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Donation entity - food, clothes, books, or essentials.
 */
@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String location;

    @Column(name = "prepared_time")
    private LocalDateTime preparedTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "veg_type")
    private VegType vegType = VegType.NA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status = DonationStatus.PENDING;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public DonationType getType() { return type; }
    public void setType(DonationType type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getPreparedTime() { return preparedTime; }
    public void setPreparedTime(LocalDateTime preparedTime) { this.preparedTime = preparedTime; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public VegType getVegType() { return vegType; }
    public void setVegType(VegType vegType) { this.vegType = vegType; }

    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }

    public Long getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(Long verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
