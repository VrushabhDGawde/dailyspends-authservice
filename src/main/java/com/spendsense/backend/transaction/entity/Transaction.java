package com.spendsense.backend.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String sender;

    @Column(name = "sms_body", columnDefinition = "TEXT")
    private String smsBody;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "payment_mode", length = 50)
    private String paymentMode;

    @Column(name = "account_ref", length = 100)
    private String accountRef;

    @Column(name = "merchant_raw", length = 255)
    private String merchantRaw;

    @Column(name = "merchant_clean", length = 255)
    private String merchantClean;

    @Column(length = 100)
    private String category;

    @Column(name = "post_balance", precision = 15, scale = 2)
    private BigDecimal postBalance;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(name = "counterparty_type", length = 50)
    private String counterpartyType;

    @Column(name = "upi_ref", length = 100)
    private String upiRef;

    @Column(name = "is_reviewed")
    private Boolean isReviewed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
