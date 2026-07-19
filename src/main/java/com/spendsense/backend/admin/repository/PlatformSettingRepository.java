package com.spendsense.backend.admin.repository;

import com.spendsense.backend.admin.entity.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, Long> {
}
