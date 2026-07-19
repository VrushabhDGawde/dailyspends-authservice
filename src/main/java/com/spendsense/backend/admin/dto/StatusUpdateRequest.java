package com.spendsense.backend.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "Status (enabled) is required")
    private Boolean enabled;
}
