package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditLogDto {
    private String username;
    private String action;
    private String targetType;
    private String targetValue;
    private String description;
    private LocalDateTime createdAt;
}
