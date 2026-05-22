package com.hospital.service;

import com.hospital.dto.AuditLogDto;

import java.util.List;

public interface AuditLogService {
    void log(String username, String action, String targetType, String targetValue, String description);

    List<AuditLogDto> getRecentLogs();

    List<AuditLogDto> getAllLogs();
}
