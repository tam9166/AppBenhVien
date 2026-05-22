package com.hospital.entity;

/**
 * Phân loại cảnh báo trong hệ thống.
 */
public enum AlertType {
    LOW_STOCK,
    EXPIRING_BATCH,
    CONSUMPTION_RISK,
    UNUSUAL_OUTBOUND,
    APPOINTMENT_REQUEST,
    APPOINTMENT_REMINDER
}
