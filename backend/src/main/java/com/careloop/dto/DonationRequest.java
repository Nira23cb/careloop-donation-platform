package com.careloop.dto;

import com.careloop.model.DonationType;
import com.careloop.model.VegType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Create donation request */
public class DonationRequest {

    @NotNull
    private DonationType type;

    @NotNull @Min(1)
    private Integer quantity;

    @NotBlank
    private String location;

    /** ISO datetime string for food prepared time */
    private String preparedTime;

    private VegType vegType = VegType.NA;

    public DonationType getType() { return type; }
    public void setType(DonationType type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPreparedTime() { return preparedTime; }
    public void setPreparedTime(String preparedTime) { this.preparedTime = preparedTime; }

    public VegType getVegType() { return vegType; }
    public void setVegType(VegType vegType) { this.vegType = vegType; }
}
