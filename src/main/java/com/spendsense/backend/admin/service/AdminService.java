package com.spendsense.backend.admin.service;

import com.spendsense.backend.admin.dto.UserResponseDTO;
import com.spendsense.backend.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    
    UserResponseDTO getUserById(Long id);
    
    UserResponseDTO updateUserStatus(Long id, boolean enabled);
    
    UserResponseDTO updateUserRole(Long id, Role role);
    
    void deleteUser(Long id);
}
