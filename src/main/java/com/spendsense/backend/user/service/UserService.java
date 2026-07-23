package com.spendsense.backend.user.service;

import com.spendsense.backend.user.dto.UserProfileDTO;

public interface UserService {
    UserProfileDTO getProfile(String email);
    UserProfileDTO updateProfile(String email, UserProfileDTO request);
}
