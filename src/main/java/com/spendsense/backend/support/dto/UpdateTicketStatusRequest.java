package com.spendsense.backend.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTicketStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
    private String adminNotes;
}
