package com.spendsense.backend.commitment.service.impl;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.commitment.dto.CommitmentDTO;
import com.spendsense.backend.commitment.entity.Commitment;
import com.spendsense.backend.commitment.repository.CommitmentRepository;
import com.spendsense.backend.commitment.service.CommitmentService;
import com.spendsense.backend.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitmentServiceImpl implements CommitmentService {

    private final CommitmentRepository commitmentRepository;
    private final AppUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommitmentDTO> getUserCommitments(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return commitmentRepository.findByUserIdOrderByDueDayAsc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommitmentDTO addCommitment(String email, CommitmentDTO request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Commitment commitment = Commitment.builder()
                .userId(user.getId())
                .name(request.getName())
                .amount(request.getAmount())
                .type(request.getType() != null ? request.getType() : "subscription")
                .frequency(request.getFrequency() != null ? request.getFrequency() : "monthly")
                .dueDay(request.getDueDay() != null ? request.getDueDay() : 1)
                .category(request.getCategory() != null ? request.getCategory() : "General")
                .totalTenureMonths(request.getTotalTenureMonths())
                .paidInstallments(request.getPaidInstallments())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .notes(request.getNotes())
                .build();

        Commitment saved = commitmentRepository.save(commitment);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public CommitmentDTO updateCommitment(String email, Long id, CommitmentDTO request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Commitment commitment = commitmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commitment not found"));

        if (!commitment.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this commitment");
        }

        if (request.getName() != null) commitment.setName(request.getName());
        if (request.getAmount() != null) commitment.setAmount(request.getAmount());
        if (request.getType() != null) commitment.setType(request.getType());
        if (request.getFrequency() != null) commitment.setFrequency(request.getFrequency());
        if (request.getDueDay() != null) commitment.setDueDay(request.getDueDay());
        if (request.getCategory() != null) commitment.setCategory(request.getCategory());
        if (request.getTotalTenureMonths() != null) commitment.setTotalTenureMonths(request.getTotalTenureMonths());
        if (request.getPaidInstallments() != null) commitment.setPaidInstallments(request.getPaidInstallments());
        if (request.getIsActive() != null) commitment.setIsActive(request.getIsActive());
        if (request.getNotes() != null) commitment.setNotes(request.getNotes());

        Commitment updated = commitmentRepository.save(commitment);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCommitment(String email, Long id) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Commitment commitment = commitmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commitment not found"));

        if (!commitment.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this commitment");
        }

        commitmentRepository.delete(commitment);
    }

    private CommitmentDTO mapToDTO(Commitment entity) {
        return CommitmentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .amount(entity.getAmount())
                .type(entity.getType())
                .frequency(entity.getFrequency())
                .dueDay(entity.getDueDay())
                .category(entity.getCategory())
                .totalTenureMonths(entity.getTotalTenureMonths())
                .paidInstallments(entity.getPaidInstallments())
                .isActive(entity.getIsActive())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
