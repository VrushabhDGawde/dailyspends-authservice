package com.spendsense.backend.auth.repository;

import com.spendsense.backend.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUuid(UUID uuid);

    boolean existsByEmail(String email);
}