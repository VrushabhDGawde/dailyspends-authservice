package com.spendsense.backend.transaction.dto;

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
public class TransactionDTO {
    private Long id;
    private String sender;
    private String smsBody;
    private Instant receivedAt;
    private BigDecimal amount;
    private String transactionType;
    private String paymentMode;
    private String accountRef;
    private String merchantRaw;
    private String merchantClean;
    private String category;
    private BigDecimal postBalance;
    private Instant transactionDate;
    private Boolean isRecurring;
    private String counterpartyType;
    private String upiRef;
    private Boolean isReviewed;
}
