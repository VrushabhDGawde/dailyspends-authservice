package com.spendsense.backend.admin.service.impl;

import com.spendsense.backend.admin.dto.UserResponseDTO;
import com.spendsense.backend.admin.service.AdminService;
import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.common.enums.Role;
import com.spendsense.backend.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AppUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return UserResponseDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long id, boolean enabled) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(Long id, Role role) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setRole(role);
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
