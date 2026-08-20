package com.spendsense.backend.transaction.service;

import com.spendsense.backend.transaction.dto.SmsIngestRequest;
import com.spendsense.backend.transaction.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionDTO> getUserTransactions(String email);
    List<TransactionDTO> getUnreviewedTransactions(String email);
    TransactionDTO addTransaction(String email, TransactionDTO request);
    List<TransactionDTO> addTransactionsBatch(String email, List<TransactionDTO> requests);
    TransactionDTO ingestSms(String email, SmsIngestRequest request);
    TransactionDTO markAsReviewed(String email, Long id, boolean reviewed);
    TransactionDTO updateTransaction(String email, Long id, TransactionDTO request);
    void deleteTransaction(String email, Long id);
}

