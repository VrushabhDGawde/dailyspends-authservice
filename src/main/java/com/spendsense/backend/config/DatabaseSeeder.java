package com.spendsense.backend.config;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            if (userRepository.findByEmail("admin@dailyspends.com").isEmpty()) {
                log.info("Admin user not found. Seeding dummy data...");

                // 1. Create Super Admin
                AppUser admin = AppUser.builder()
                        .uuid(UUID.randomUUID())
                        .fullName("Super Admin")
                        .email("admin@dailyspends.com")
                        .password(passwordEncoder.encode("admin"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                // 2. Create Normal Users
                AppUser user1 = AppUser.builder()
                        .uuid(UUID.randomUUID())
                        .fullName("John Doe")
                        .email("john@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(Role.USER)
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(Instant.now().minusSeconds(86400 * 2)) // 2 days ago
                        .updatedAt(Instant.now().minusSeconds(86400 * 2))
                        .build();

                AppUser user2 = AppUser.builder()
                        .uuid(UUID.randomUUID())
                        .fullName("Jane Smith")
                        .email("jane@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(Role.USER)
                        .enabled(true)
                        .emailVerified(true)
                        .createdAt(Instant.now().minusSeconds(86400 * 5)) // 5 days ago
                        .updatedAt(Instant.now().minusSeconds(86400 * 5))
                        .build();

                AppUser user3 = AppUser.builder()
                        .uuid(UUID.randomUUID())
                        .fullName("Blocked User")
                        .email("blocked@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(Role.USER)
                        .enabled(false) // Blocked
                        .emailVerified(false)
                        .createdAt(Instant.now().minusSeconds(86400 * 10)) // 10 days ago
                        .updatedAt(Instant.now().minusSeconds(86400 * 10))
                        .build();

                userRepository.saveAll(List.of(admin, user1, user2, user3));
                log.info("Database seeding completed! Admin User: admin@dailyspends.com / admin");
            } else {
                log.info("Admin user already exists. Skipping seeding.");
            }
        };
    }
}
