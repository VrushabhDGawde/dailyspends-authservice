package com.spendsense.backend.transaction.service;

import com.spendsense.backend.transaction.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SmsParserServiceTest {

    private SmsParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new SmsParserService();
    }

    @Test
    @DisplayName("Should parse standard HDFC debit SMS and set isReviewed to false")
    void testParseHdfcDebitSms() {
        String sms = "Rs 450.00 debited from A/c XX1234 on 20-Aug-26 at Zomato. UPI: 987654321012. Avl Bal: Rs 15,200.50";
        Transaction tx = Transaction.builder().smsBody(sms).build();

        Transaction parsed = parserService.parseAndPopulate(tx);

        assertThat(parsed.getAmount()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(parsed.getTransactionType()).isEqualTo("DEBIT");
        assertThat(parsed.getPaymentMode()).isEqualTo("UPI");
        assertThat(parsed.getUpiRef()).isEqualTo("987654321012");
        assertThat(parsed.getAccountRef()).isEqualTo("XX1234");
        assertThat(parsed.getMerchantClean()).isEqualTo("Zomato");
        assertThat(parsed.getCategory()).isEqualTo("Food & Dining");
        assertThat(parsed.getPostBalance()).isEqualByComparingTo(new BigDecimal("15200.50"));
        assertThat(parsed.getIsReviewed()).isFalse();
    }

    @Test
    @DisplayName("Should parse credit P2P transfer correctly")
    void testParseCreditP2pSms() {
        String sms = "INR 2,500.00 credited to your Account XX9988; Rahul Sharma credited via UPI/112233445566.";
        Transaction tx = Transaction.builder().smsBody(sms).build();

        Transaction parsed = parserService.parseAndPopulate(tx);

        assertThat(parsed.getAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(parsed.getTransactionType()).isEqualTo("CREDIT");
        assertThat(parsed.getCounterpartyType()).isEqualTo("INDIVIDUAL");
        assertThat(parsed.getMerchantRaw()).isEqualTo("Rahul Sharma");
        assertThat(parsed.getIsReviewed()).isFalse();
    }
}
