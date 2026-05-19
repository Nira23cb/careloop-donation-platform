package com.careloop.dto;

import jakarta.validation.constraints.NotBlank;

/** Cancellation must include a reason */
public class CancelRequest {

    @NotBlank
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
