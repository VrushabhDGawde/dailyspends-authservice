package com.spendsense.backend.transaction.service.impl;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.common.exception.UserNotFoundException;
import com.spendsense.backend.transaction.dto.SmsIngestRequest;
import com.spendsense.backend.transaction.dto.TransactionDTO;
import com.spendsense.backend.transaction.entity.Transaction;
import com.spendsense.backend.transaction.repository.TransactionRepository;
import com.spendsense.backend.transaction.service.SmsParserService;
import com.spendsense.backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AppUserRepository userRepository;
    private final SmsParserService smsParserService;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getUserTransactions(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return transactionRepository.findByUserIdOrderByTransactionDateDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getUnreviewedTransactions(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return transactionRepository.findByUserIdAndIsReviewedFalseOrderByTransactionDateDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDTO ingestSms(String email, SmsIngestRequest request) {
        String targetEmail = (email != null && !email.isBlank()) ? email : request.getUserEmail();
        AppUser user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetEmail));

        Transaction transaction = Transaction.builder()
                .userId(user.getId())
                .sender(request.getSender())
                .smsBody(request.getSmsBody())
                .receivedAt(Instant.now())
                .build();

        // 🧠 Auto-parse with regex engine
        transaction = smsParserService.parseAndPopulate(transaction);
        transaction.setUserId(user.getId());
        transaction.setIsReviewed(false);

        Transaction saved = transactionRepository.save(transaction);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TransactionDTO markAsReviewed(String email, Long id, boolean reviewed) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this transaction");
        }

        transaction.setIsReviewed(reviewed);
        Transaction updated = transactionRepository.save(transaction);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public TransactionDTO addTransaction(String email, TransactionDTO request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Transaction transaction = Transaction.builder()
                .userId(user.getId())
                .sender(request.getSender())
                .smsBody(request.getSmsBody())
                .receivedAt(request.getReceivedAt())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .paymentMode(request.getPaymentMode())
                .accountRef(request.getAccountRef())
                .merchantRaw(request.getMerchantRaw())
                .merchantClean(request.getMerchantClean())
                .category(request.getCategory())
                .postBalance(request.getPostBalance())
                .transactionDate(request.getTransactionDate())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .counterpartyType(request.getCounterpartyType())
                .upiRef(request.getUpiRef())
                .isReviewed(request.getIsReviewed() != null ? request.getIsReviewed() : false)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public List<TransactionDTO> addTransactionsBatch(String email, List<TransactionDTO> requests) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Transaction> entities = new ArrayList<>();
        for (TransactionDTO request : requests) {
            Transaction transaction = Transaction.builder()
                    .userId(user.getId())
                    .sender(request.getSender())
                    .smsBody(request.getSmsBody())
                    .receivedAt(request.getReceivedAt())
                    .amount(request.getAmount())
                    .transactionType(request.getTransactionType())
                    .paymentMode(request.getPaymentMode())
                    .accountRef(request.getAccountRef())
                    .merchantRaw(request.getMerchantRaw())
                    .merchantClean(request.getMerchantClean())
                    .category(request.getCategory())
                    .postBalance(request.getPostBalance())
                    .transactionDate(request.getTransactionDate())
                    .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                    .counterpartyType(request.getCounterpartyType())
                    .upiRef(request.getUpiRef())
                    .isReviewed(request.getIsReviewed() != null ? request.getIsReviewed() : false)
                    .build();
            entities.add(transaction);
        }

        List<Transaction> saved = transactionRepository.saveAll(entities);
        return saved.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(String email, Long id, TransactionDTO request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this transaction");
        }

        if (request.getIsReviewed() != null) transaction.setIsReviewed(request.getIsReviewed());
        if (request.getCategory() != null) transaction.setCategory(request.getCategory());
        if (request.getMerchantClean() != null) transaction.setMerchantClean(request.getMerchantClean());
        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getTransactionType() != null) transaction.setTransactionType(request.getTransactionType());
        if (request.getTransactionDate() != null) transaction.setTransactionDate(request.getTransactionDate());
        
        Transaction updated = transactionRepository.save(transaction);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(String email, Long id) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this transaction");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionDTO mapToDTO(Transaction entity) {
        return TransactionDTO.builder()
                .id(entity.getId())
                .sender(entity.getSender())
                .smsBody(entity.getSmsBody())
                .receivedAt(entity.getReceivedAt())
                .amount(entity.getAmount())
                .transactionType(entity.getTransactionType())
                .paymentMode(entity.getPaymentMode())
                .accountRef(entity.getAccountRef())
                .merchantRaw(entity.getMerchantRaw())
                .merchantClean(entity.getMerchantClean())
                .category(entity.getCategory())
                .postBalance(entity.getPostBalance())
                .transactionDate(entity.getTransactionDate())
                .isRecurring(entity.getIsRecurring())
                .counterpartyType(entity.getCounterpartyType())
                .upiRef(entity.getUpiRef())
                .isReviewed(entity.getIsReviewed())
                .build();
    }
}
