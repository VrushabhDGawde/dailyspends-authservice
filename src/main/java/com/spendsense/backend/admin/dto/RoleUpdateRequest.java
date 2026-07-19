package com.spendsense.backend.admin.dto;

import com.spendsense.backend.common.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull(message = "Role is required")
    private Role role;
}
