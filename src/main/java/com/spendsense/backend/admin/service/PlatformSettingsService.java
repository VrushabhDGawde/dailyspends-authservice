package com.spendsense.backend.admin.service;

import com.spendsense.backend.admin.dto.PlatformSettingsDTO;
import com.spendsense.backend.admin.entity.PlatformSetting;
import com.spendsense.backend.admin.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformSettingsService {

    private final PlatformSettingRepository repository;

    public PlatformSettingsDTO getSettings() {
        List<PlatformSetting> settingsList = repository.findAll();
        PlatformSetting settings;
        if (settingsList.isEmpty()) {
            settings = PlatformSetting.builder()
                    .websiteTitle("DailySpends")
                    .themeMode("dark")
                    .primaryColor("#ef4444")
                    .build();
            settings = repository.save(settings);
        } else {
            settings = settingsList.get(0);
        }

        return PlatformSettingsDTO.builder()
                .websiteTitle(settings.getWebsiteTitle())
                .themeMode(settings.getThemeMode())
                .primaryColor(settings.getPrimaryColor())
                .build();
    }

    public PlatformSettingsDTO updateSettings(PlatformSettingsDTO dto) {
        List<PlatformSetting> settingsList = repository.findAll();
        PlatformSetting settings;
        if (settingsList.isEmpty()) {
            settings = new PlatformSetting();
        } else {
            settings = settingsList.get(0);
        }

        settings.setWebsiteTitle(dto.getWebsiteTitle());
        settings.setThemeMode(dto.getThemeMode());
        settings.setPrimaryColor(dto.getPrimaryColor());

        settings = repository.save(settings);

        return PlatformSettingsDTO.builder()
                .websiteTitle(settings.getWebsiteTitle())
                .themeMode(settings.getThemeMode())
                .primaryColor(settings.getPrimaryColor())
                .build();
    }
}
