package com.spendsense.backend.transaction.controller;

import com.spendsense.backend.transaction.dto.SmsIngestRequest;
import com.spendsense.backend.transaction.dto.TransactionDTO;
import com.spendsense.backend.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactions(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getUserTransactions(authentication.getName()));
    }

    @GetMapping("/unreviewed")
    public ResponseEntity<List<TransactionDTO>> getUnreviewedTransactions(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getUnreviewedTransactions(authentication.getName()));
    }

    @PostMapping("/ingest")
    public ResponseEntity<TransactionDTO> ingestSms(
            Authentication authentication,
            @Valid @RequestBody SmsIngestRequest request) {
        String email = authentication != null ? authentication.getName() : request.getUserEmail();
        return new ResponseEntity<>(transactionService.ingestSms(email, request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<TransactionDTO> markReviewed(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean reviewed) {
        return ResponseEntity.ok(transactionService.markAsReviewed(authentication.getName(), id, reviewed));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> addTransaction(
            Authentication authentication,
            @RequestBody TransactionDTO request) {
        return new ResponseEntity<>(transactionService.addTransaction(authentication.getName(), request), HttpStatus.CREATED);
    }

    @PostMapping("/manual")
    public ResponseEntity<TransactionDTO> addTransactionManual(
            Authentication authentication,
            @RequestBody TransactionDTO request) {
        return new ResponseEntity<>(transactionService.addTransaction(authentication.getName(), request), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TransactionDTO>> addTransactionsBatch(
            Authentication authentication,
            @RequestBody List<TransactionDTO> requests) {
        return new ResponseEntity<>(transactionService.addTransactionsBatch(authentication.getName(), requests), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TransactionDTO request) {
        return ResponseEntity.ok(transactionService.updateTransaction(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            Authentication authentication,
            @PathVariable Long id) {
        transactionService.deleteTransaction(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
