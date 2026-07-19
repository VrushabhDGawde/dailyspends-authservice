package com.spendsense.backend.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDTO {
    private Long id;
    private String ticketNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private String subject;
    private String category;
    private String priority;
    private String status;
    private String description;
    private String adminNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
