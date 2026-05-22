package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "appointment_requests")
public class AppointmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vui lÃ²ng nháº­p há» tÃªn")
    @Column(name = "patient_name", nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String patientName;

    @NotBlank(message = "Vui lÃ²ng nháº­p sá»‘ Ä‘iá»‡n thoáº¡i")
    @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡")
    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Email(message = "Email khÃ´ng há»£p lá»‡")
    @Column(name = "email", length = 120)
    private String email;

    @NotBlank(message = "Vui lÃ²ng chá»n khoa")
    @Column(name = "department", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String department;

    @NotNull(message = "Vui lÃ²ng chá»n ngÃ y khÃ¡m")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Vui lÃ²ng chá»n giá» khÃ¡m")
    @DateTimeFormat(pattern = "HH:mm")
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @Size(max = 1000, message = "MÃ´ táº£ triá»‡u chá»©ng tá»‘i Ä‘a 1000 kÃ½ tá»±")
    @Column(name = "symptoms", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String symptoms;

    @Column(name = "screening_ticket_id")
    private Long screeningTicketId;

    @Column(name = "triage_summary", length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String triageSummary;

    @Column(name = "priority_level", length = 50, columnDefinition = "NVARCHAR(50)")
    private String priorityLevel;

    @Column(name = "emergency", nullable = false)
    private boolean emergency;

    @Column(name = "queue_number")
    private Integer queueNumber;

    @Column(name = "admin_note", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String adminNote;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Transient
    @AssertTrue(message = "NgÃ y khÃ¡m pháº£i tá»« hÃ´m nay trá»Ÿ Ä‘i")
    public boolean isAppointmentDateValid() {
        return appointmentDate == null || !appointmentDate.isBefore(LocalDate.now());
    }

    @Transient
    @AssertTrue(message = "Bá»‡nh viá»‡n nháº­n lá»‹ch online tá»« thá»© 2 Ä‘áº¿n thá»© 7")
    public boolean isAppointmentDayAllowed() {
        return appointmentDate == null || appointmentDate.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    @Transient
    @AssertTrue(message = "Giá» khÃ¡m pháº£i trong khung 07:00 - 16:30")
    public boolean isAppointmentTimeValid() {
        if (appointmentTime == null) {
            return true;
        }
        LocalTime start = LocalTime.of(7, 0);
        LocalTime end = LocalTime.of(16, 30);
        return !appointmentTime.isBefore(start) && !appointmentTime.isAfter(end);
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (qrCode == null || qrCode.isBlank()) {
            qrCode = "APPT-" + UUID.randomUUID();
        }
        if (priorityLevel == null || priorityLevel.isBlank()) {
            priorityLevel = "Thông thường";
        }
    }
}

