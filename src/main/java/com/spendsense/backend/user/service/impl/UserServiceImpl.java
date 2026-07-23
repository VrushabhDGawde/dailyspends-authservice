package com.spendsense.backend.user.service.impl;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.common.exception.UserNotFoundException;
import com.spendsense.backend.user.dto.UserProfileDTO;
import com.spendsense.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;

    @Override
    public UserProfileDTO getProfile(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(String email, UserProfileDTO request) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        
        if (request.getSalary() != null) {
            user.setSalary(request.getSalary());
        }

        if (request.getSavingsPercentage() != null) {
            user.setSavingsPercentage(request.getSavingsPercentage());
        }

        if (request.getDob() != null && !request.getDob().trim().isEmpty()) {
            user.setDob(request.getDob().trim());
        }

        if (request.getOccupation() != null && !request.getOccupation().trim().isEmpty()) {
            user.setOccupation(request.getOccupation().trim());
        }

        if (Boolean.TRUE.equals(request.getIsProfileComplete())) {
            user.setIsProfileComplete(true);
        } else if (user.getSalary() != null && user.getSalary() > 0 &&
                   user.getSavingsPercentage() != null &&
                   user.getDob() != null && !user.getDob().trim().isEmpty() &&
                   user.getOccupation() != null && !user.getOccupation().trim().isEmpty()) {
            user.setIsProfileComplete(true);
        }

        userRepository.save(user);
        return mapToDTO(user);
    }

    private UserProfileDTO mapToDTO(AppUser user) {
        boolean isComplete = Boolean.TRUE.equals(user.getIsProfileComplete()) ||
                (user.getSalary() != null && user.getSalary() > 0 &&
                 user.getSavingsPercentage() != null &&
                 user.getDob() != null && !user.getDob().trim().isEmpty() &&
                 user.getOccupation() != null && !user.getOccupation().trim().isEmpty());

        return UserProfileDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .salary(user.getSalary())
                .savingsPercentage(user.getSavingsPercentage())
                .dob(user.getDob())
                .occupation(user.getOccupation())
                .isProfileComplete(isComplete)
                .build();
    }
}
