package com.hospital.service.impl;

import com.hospital.dto.AuditLogDto;
import com.hospital.entity.AuditLog;
import com.hospital.repository.AuditLogRepository;
import com.hospital.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(String username, String action, String targetType, String targetValue, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetValue(targetValue);
        auditLog.setDescription(description);
        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogDto> getRecentLogs() {
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(1);
        return auditLogRepository.findRecentLogs(fromDate).stream()
                .limit(8)
                .map(log -> new AuditLogDto(
                        log.getUsername(),
                        log.getAction(),
                        log.getTargetType(),
                        log.getTargetValue(),
                        log.getDescription(),
                        log.getCreatedAt()))
                .toList();
    }

    @Override
    public List<AuditLogDto> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(log -> new AuditLogDto(
                        log.getUsername(),
                        log.getAction(),
                        log.getTargetType(),
                        log.getTargetValue(),
                        log.getDescription(),
                        log.getCreatedAt()))
                .toList();
    }
}
