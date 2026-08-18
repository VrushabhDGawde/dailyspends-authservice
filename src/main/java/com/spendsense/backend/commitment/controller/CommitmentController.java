package com.spendsense.backend.commitment.controller;

import com.spendsense.backend.commitment.dto.CommitmentDTO;
import com.spendsense.backend.commitment.service.CommitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commitments")
@RequiredArgsConstructor
public class CommitmentController {

    private final CommitmentService commitmentService;

    @GetMapping
    public ResponseEntity<List<CommitmentDTO>> getUserCommitments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commitmentService.getUserCommitments(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<CommitmentDTO> addCommitment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommitmentDTO request) {
        return new ResponseEntity<>(commitmentService.addCommitment(userDetails.getUsername(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommitmentDTO> updateCommitment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CommitmentDTO request) {
        return ResponseEntity.ok(commitmentService.updateCommitment(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommitment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        commitmentService.deleteCommitment(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
