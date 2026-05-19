package com.careloop.dto;

import com.careloop.model.*;
import java.time.LocalDateTime;

/** Donation data sent to frontend */
public class DonationResponse {

    private Long id;
    private Long donorId;
    private String donorName;
    private DonationType type;
    private Integer quantity;
    private String location;
    private LocalDateTime preparedTime;
    private LocalDateTime expiryTime;
    private VegType vegType;
    private DonationStatus status;
    private boolean verified;
    private boolean urgent;
    private String timeLeft;
    private Long minutesLeft;
    private Long volunteerId;
    private String volunteerName;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

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

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public String getTimeLeft() { return timeLeft; }
    public void setTimeLeft(String timeLeft) { this.timeLeft = timeLeft; }

    public Long getMinutesLeft() { return minutesLeft; }
    public void setMinutesLeft(Long minutesLeft) { this.minutesLeft = minutesLeft; }

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
