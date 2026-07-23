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
        
        user.setSalary(request.getSalary());
        user.setSavingsPercentage(request.getSavingsPercentage());

        userRepository.save(user);
        return mapToDTO(user);
    }

    private UserProfileDTO mapToDTO(AppUser user) {
        return UserProfileDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .salary(user.getSalary())
                .savingsPercentage(user.getSavingsPercentage())
                .build();
    }
}
