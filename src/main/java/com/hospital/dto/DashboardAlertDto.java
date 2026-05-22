package com.hospital.dto;

import com.hospital.entity.AlertType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DashboardAlertDto {
    private AlertType alertType;
    private String title;
    private String message;
    private String referenceCode;
    private LocalDateTime lastDetectedAt;
}
