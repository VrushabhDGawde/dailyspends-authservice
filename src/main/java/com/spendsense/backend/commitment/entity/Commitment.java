package com.spendsense.backend.commitment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "commitments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String type; // emi, subscription, bill

    @Column(nullable = false, length = 50)
    private String frequency; // monthly, yearly, quarterly, weekly

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "total_tenure_months")
    private Integer totalTenureMonths;

    @Column(name = "paid_installments")
    private Integer paidInstallments;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (isActive == null) isActive = true;
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
