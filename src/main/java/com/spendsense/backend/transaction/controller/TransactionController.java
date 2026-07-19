package com.spendsense.backend.transaction.controller;

import com.spendsense.backend.transaction.dto.TransactionDTO;
import com.spendsense.backend.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/manual")
    public ResponseEntity<TransactionDTO> addTransaction(
            Authentication authentication,
            @RequestBody TransactionDTO request) {
        return ResponseEntity.ok(transactionService.addTransaction(authentication.getName(), request));
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
