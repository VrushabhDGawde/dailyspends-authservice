package com.spendsense.backend.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Category is required")
    private String category;

    private String priority = "MEDIUM";

    @NotBlank(message = "Description is required")
    private String description;
}
