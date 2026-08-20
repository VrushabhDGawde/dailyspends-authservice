package com.spendsense.backend.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsIngestRequest {

    private String sender;

    @NotBlank(message = "SMS body is required")
    private String smsBody;

    private String userEmail;
}
