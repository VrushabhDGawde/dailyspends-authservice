package com.spendsense.backend.admin.dto;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.common.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private UUID uuid;
    private String fullName;
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean emailVerified;
    private Instant createdAt;

    public static UserResponseDTO fromEntity(AppUser user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
