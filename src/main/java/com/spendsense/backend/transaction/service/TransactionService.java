package com.spendsense.backend.transaction.service;

import com.spendsense.backend.transaction.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionDTO> getUserTransactions(String email);
    TransactionDTO addTransaction(String email, TransactionDTO request);
    List<TransactionDTO> addTransactionsBatch(String email, List<TransactionDTO> requests);
    TransactionDTO updateTransaction(String email, Long id, TransactionDTO request);
    void deleteTransaction(String email, Long id);
}
