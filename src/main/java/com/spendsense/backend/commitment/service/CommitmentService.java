package com.spendsense.backend.commitment.service;

import com.spendsense.backend.commitment.dto.CommitmentDTO;

import java.util.List;

public interface CommitmentService {
    List<CommitmentDTO> getUserCommitments(String email);
    CommitmentDTO addCommitment(String email, CommitmentDTO request);
    CommitmentDTO updateCommitment(String email, Long id, CommitmentDTO request);
    void deleteCommitment(String email, Long id);
}
