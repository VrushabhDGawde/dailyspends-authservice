package com.spendsense.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppleLoginRequest {

    @NotBlank(message = "ID Token is required")
    private String idToken;

    private String userIdentifier;
    private String email;
    private String fullName;
}
