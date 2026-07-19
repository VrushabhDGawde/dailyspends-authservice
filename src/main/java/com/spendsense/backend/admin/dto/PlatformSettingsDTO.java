package com.spendsense.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingsDTO {
    private String websiteTitle;
    private String themeMode;
    private String primaryColor;
}
