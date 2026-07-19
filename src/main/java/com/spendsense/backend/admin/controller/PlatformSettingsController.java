package com.spendsense.backend.admin.controller;

import com.spendsense.backend.admin.dto.PlatformSettingsDTO;
import com.spendsense.backend.admin.service.PlatformSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsService service;

    // Public endpoint for frontend to fetch branding/theme
    @GetMapping("/settings")
    public ResponseEntity<PlatformSettingsDTO> getSettings() {
        return ResponseEntity.ok(service.getSettings());
    }

    // Admin only endpoint to update branding/theme
    @PutMapping("/v1/admin/settings")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PlatformSettingsDTO> updateSettings(@RequestBody PlatformSettingsDTO dto) {
        return ResponseEntity.ok(service.updateSettings(dto));
    }
}
