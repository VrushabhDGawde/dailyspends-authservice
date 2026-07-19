package com.spendsense.backend.security.service;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String emailToFind = "admin".equalsIgnoreCase(username) ? "admin@dailyspends.com" : username;

        AppUser appUser = appUserRepository.findByEmail(emailToFind)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email/username: " + username));

        return new UserPrincipal(appUser);
    }
}