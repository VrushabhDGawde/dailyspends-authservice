package com.spendsense.backend.transaction.service;

import com.spendsense.backend.transaction.entity.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmsParserService {

    public Transaction parseAndPopulate(Transaction transaction) {
        String body = transaction.getSmsBody();
        if (body == null || body.isBlank()) {
            if (transaction.getAmount() == null) transaction.setAmount(BigDecimal.ZERO);
            if (transaction.getTransactionType() == null) transaction.setTransactionType("DEBIT");
            if (transaction.getPaymentMode() == null) transaction.setPaymentMode("UPI");
            if (transaction.getCategory() == null) transaction.setCategory("General");
            if (transaction.getTransactionDate() == null) transaction.setTransactionDate(Instant.now());
            if (transaction.getIsReviewed() == null) transaction.setIsReviewed(false);
            return transaction;
        }

        // 1. Amount Extraction (supports Rs., INR, with commas & decimals)
        Pattern amountPattern = Pattern.compile("(?i)(?:Rs\\.?|INR)\\s*([\\d,]+(?:\\.\\d{1,2})?)");
        Matcher amountMatcher = amountPattern.matcher(body);
        if (amountMatcher.find()) {
            String amtStr = amountMatcher.group(1).replace(",", "");
            transaction.setAmount(new BigDecimal(amtStr));
        } else if (transaction.getAmount() == null) {
            transaction.setAmount(BigDecimal.ZERO);
        }

        // 2. Transaction Type Classification (DEBIT vs CREDIT)
        String lowerBody = body.toLowerCase();
        if (lowerBody.contains("debited") || lowerBody.contains("spent") || lowerBody.contains("used at") || lowerBody.contains("paid")) {
            transaction.setTransactionType("DEBIT");
        } else if (lowerBody.contains("credited") || lowerBody.contains("received") || lowerBody.contains("deposited")) {
            transaction.setTransactionType("CREDIT");
        } else if (transaction.getTransactionType() == null) {
            transaction.setTransactionType("DEBIT");
        }

        // 3. Payment Mode Auto-Detection
        if (lowerBody.contains("upi") || lowerBody.contains("vpa") || lowerBody.contains("gpay") || lowerBody.contains("phonepe") || lowerBody.contains("paytm")) {
            transaction.setPaymentMode("UPI");
        } else if (lowerBody.contains("pos") || lowerBody.contains("card") || lowerBody.contains("debit card") || lowerBody.contains("credit card")) {
            transaction.setPaymentMode("CARD");
        } else if (lowerBody.contains("atm")) {
            transaction.setPaymentMode("ATM");
        } else if (lowerBody.contains("netbanking") || lowerBody.contains("neft") || lowerBody.contains("rtgs") || lowerBody.contains("imps")) {
            transaction.setPaymentMode("NETBANKING");
        } else if (transaction.getPaymentMode() == null) {
            transaction.setPaymentMode("UPI");
        }

        // 4. UPI Reference Number (RRN) Extraction
        Pattern upiPattern = Pattern.compile("(?i)UPI\\s*[:\\s/-]?\\s*(\\d+)");
        Matcher upiMatcher = upiPattern.matcher(body);
        if (upiMatcher.find()) {
            transaction.setUpiRef(upiMatcher.group(1));
            transaction.setPaymentMode("UPI");
        }

        // 5. Account Reference Mask Extraction (A/c XX1234, acct ending 4567)
        Pattern acctPattern = Pattern.compile("(?i)(?:acct|ac|a/c)\\s*(?:ending)?\\s*([X\\d]+)");
        Matcher acctMatcher = acctPattern.matcher(body);
        if (acctMatcher.find()) {
            transaction.setAccountRef(acctMatcher.group(1));
        }

        // 6. Counterparty / Merchant Extraction Engine
        String extractedCounterparty = "General";
        String type = "MERCHANT";

        String brand = findKnownBrand(lowerBody);
        if (brand != null) {
            extractedCounterparty = brand;
        } else if (lowerBody.contains("credited") && body.contains(";")) {
            Pattern p2pPattern = Pattern.compile("(?i);\\s*([A-Za-z\\s]+?)\\s+credited");
            Matcher p2pMatcher = p2pPattern.matcher(body);
            if (p2pMatcher.find()) {
                extractedCounterparty = p2pMatcher.group(1).trim();
                type = "INDIVIDUAL";
            }
        } else if (lowerBody.contains("from ")) {
            Pattern fromPattern = Pattern.compile("(?i)from\\s+([A-Za-z0-9\\s*#-]+?)(?:\\s+on|\\s+bal|\\.|$)");
            Matcher fromMatcher = fromPattern.matcher(body);
            if (fromMatcher.find()) {
                extractedCounterparty = fromMatcher.group(1).trim();
            }
        } else {
            Pattern generalPattern = Pattern.compile("(?i)(?:to|at|spent on|paid to)\\s+(?!your\\s+ac|ac\\s+)([A-Za-z0-9\\s*#-]+?)(?:\\s+on|\\s+vpa|\\s+bal|\\.|$)");
            Matcher generalMatcher = generalPattern.matcher(body);
            if (generalMatcher.find()) {
                extractedCounterparty = generalMatcher.group(1).trim();
            }
        }

        transaction.setMerchantRaw(extractedCounterparty);
        transaction.setMerchantClean(cleanMerchantName(extractedCounterparty));
        transaction.setCounterpartyType(type);
        transaction.setCategory(detectCategory(transaction.getMerchantClean(), lowerBody));

        // 7. Post Balance Extraction
        Pattern balPattern = Pattern.compile("(?i)(?:bal|balance|avbl bal|avl bal)\\s*[:\\s-]*\\s*(?:is\\s*)?(?:Rs\\.?|INR)?\\s*([\\d,]+(?:\\.\\d{1,2})?)");
        Matcher balMatcher = balPattern.matcher(body);
        if (balMatcher.find()) {
            String balStr = balMatcher.group(1).replace(",", "");
            transaction.setPostBalance(new BigDecimal(balStr));
        }

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(Instant.now());
        }
        if (transaction.getReceivedAt() == null) {
            transaction.setReceivedAt(Instant.now());
        }
        if (transaction.getIsRecurring() == null) {
            transaction.setIsRecurring(false);
        }
        transaction.setIsReviewed(false); // Ingested SMS is always unreviewed initially

        return transaction;
    }

    public String findKnownBrand(String bodyLower) {
        if (bodyLower.contains("zomato")) return "Zomato";
        if (bodyLower.contains("swiggy")) return "Swiggy";
        if (bodyLower.contains("blinkit") || bodyLower.contains("grofers")) return "Blinkit";
        if (bodyLower.contains("zepto")) return "Zepto";
        if (bodyLower.contains("amazon")) return "Amazon";
        if (bodyLower.contains("flipkart")) return "Flipkart";
        if (bodyLower.contains("starbucks")) return "Starbucks";
        if (bodyLower.contains("mcdonald")) return "McDonald's";
        if (bodyLower.contains("uber")) return "Uber";
        if (bodyLower.contains("ola")) return "Ola";
        if (bodyLower.contains("rapido")) return "Rapido";
        if (bodyLower.contains("netflix")) return "Netflix";
        if (bodyLower.contains("spotify")) return "Spotify";
        if (bodyLower.contains("youtube") || bodyLower.contains("google")) return "Google / YouTube";
        return null;
    }

    public String cleanMerchantName(String rawMerchant) {
        if (rawMerchant == null || rawMerchant.isBlank()) return "General Merchant";
        String brand = findKnownBrand(rawMerchant.toLowerCase());
        if (brand != null) return brand;
        return rawMerchant.trim();
    }

    public String detectCategory(String merchantClean, String smsBodyLower) {
        String mLower = merchantClean.toLowerCase();
        if (mLower.contains("zomato") || mLower.contains("swiggy") || mLower.contains("mcdonald") || mLower.contains("starbucks") || smsBodyLower.contains("restaurant") || smsBodyLower.contains("cafe") || smsBodyLower.contains("food")) {
            return "Food & Dining";
        }
        if (mLower.contains("blinkit") || mLower.contains("zepto") || mLower.contains("instamart") || smsBodyLower.contains("grocery") || smsBodyLower.contains("supermarket")) {
            return "Groceries";
        }
        if (mLower.contains("uber") || mLower.contains("ola") || mLower.contains("rapido") || smsBodyLower.contains("metro") || smsBodyLower.contains("fuel") || smsBodyLower.contains("petrol")) {
            return "Transportation";
        }
        if (mLower.contains("amazon") || mLower.contains("flipkart") || mLower.contains("myntra") || mLower.contains("ajio") || smsBodyLower.contains("shopping")) {
            return "Shopping";
        }
        if (mLower.contains("netflix") || mLower.contains("spotify") || mLower.contains("youtube") || mLower.contains("prime") || smsBodyLower.contains("entertainment")) {
            return "Subscriptions";
        }
        if (smsBodyLower.contains("electricity") || smsBodyLower.contains("water bill") || smsBodyLower.contains("broadband") || smsBodyLower.contains("wifi") || smsBodyLower.contains("recharge") || smsBodyLower.contains("airtel") || smsBodyLower.contains("jio")) {
            return "Bills & Utilities";
        }
        return "General";
    }
}
