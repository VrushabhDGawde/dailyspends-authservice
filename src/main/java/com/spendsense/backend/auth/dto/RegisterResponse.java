package com.spendsense.backend.auth.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private UUID userId;

    private String fullName;

    private String email;

    private String message;
}