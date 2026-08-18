package com.spendsense.backend.commitment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitmentDTO {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String type;
    private String frequency;
    private Integer dueDay;
    private String category;
    private Integer totalTenureMonths;
    private Integer paidInstallments;
    private Boolean isActive;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
