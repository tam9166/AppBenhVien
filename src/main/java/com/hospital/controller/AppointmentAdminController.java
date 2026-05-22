package com.hospital.controller;

import com.hospital.entity.AlertType;
import com.hospital.entity.AppointmentRequest;
import com.hospital.entity.AppointmentStatus;
import com.hospital.repository.AlertNotificationRepository;
import com.hospital.repository.AppointmentRequestRepository;
import com.hospital.repository.ScreeningTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentAdminController {

    private final AppointmentRequestRepository appointmentRequestRepository;
    private final ScreeningTicketRepository screeningTicketRepository;
    private final AlertNotificationRepository alertNotificationRepository;

    @GetMapping
    public String list(@RequestParam(required = false) LocalDate date,
                       @RequestParam(required = false) String status,
                       Model model) {
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        AppointmentStatus selectedStatus = status != null && !status.isBlank()
                ? AppointmentStatus.valueOf(status)
                : null;

        List<AppointmentRequest> allAppointments =
                appointmentRequestRepository.findByAppointmentDateOrderByAppointmentTimeAscCreatedAtAsc(selectedDate);
        List<AppointmentRequest> filteredAppointments = selectedStatus == null
                ? allAppointments
                : allAppointments.stream().filter(item -> item.getStatus() == selectedStatus).toList();

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedStatus", selectedStatus != null ? selectedStatus.name() : "");
        model.addAttribute("appointments", filteredAppointments);
        model.addAttribute("statusOptions", Arrays.asList(AppointmentStatus.values()));
        model.addAttribute("queueAppointments",
                appointmentRequestRepository.findTop10ByAppointmentDateAndStatusOrderByCreatedAtAsc(LocalDate.now(), AppointmentStatus.CHECKED_IN));
        model.addAttribute("nextDayReminders",
                appointmentRequestRepository.findByAppointmentDateOrderByAppointmentTimeAscCreatedAtAsc(LocalDate.now().plusDays(1)));
        model.addAttribute("departmentDemand",
                appointmentRequestRepository.summarizeDepartmentDemand(LocalDateTime.now().minusDays(30)));
        model.addAttribute("newAppointmentNotifications",
                alertNotificationRepository.findTop10ByAlertTypeAndResolvedFalseOrderByLastDetectedAtDesc(AlertType.APPOINTMENT_REQUEST));
        return "appointments/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AppointmentRequest appointment = appointmentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay lich hen"));
        model.addAttribute("appointment", appointment);
        model.addAttribute("statusOptions", Arrays.asList(AppointmentStatus.values()));
        if (appointment.getScreeningTicketId() != null) {
            screeningTicketRepository.findById(appointment.getScreeningTicketId())
                    .ifPresent(ticket -> model.addAttribute("screeningTicket", ticket));
        }
        return "appointments/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam AppointmentStatus status,
                               @RequestParam(required = false) String adminNote,
                               RedirectAttributes redirectAttributes) {
        AppointmentRequest appointment = appointmentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay lich hen"));
        appointment.setStatus(status);
        appointment.setAdminNote(adminNote);
        if (status == AppointmentStatus.CONFIRMED) {
            appointment.setConfirmedAt(LocalDateTime.now());
        } else if (status == AppointmentStatus.CANCELLED) {
            appointment.setCancelledAt(LocalDateTime.now());
        }
        appointmentRequestRepository.save(appointment);
        redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat trang thai lich hen.");
        return "redirect:/appointments/" + id;
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        AppointmentRequest appointment = appointmentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay lich hen"));
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckedInAt(LocalDateTime.now());
        if (appointment.getQueueNumber() == null) {
            appointment.setQueueNumber(resolveNextQueueNumber(appointment.getAppointmentDate()));
        }
        appointmentRequestRepository.save(appointment);
        redirectAttributes.addFlashAttribute("successMessage", "Da check-in va dua benh nhan vao danh sach cho.");
        return "redirect:/appointments/" + id;
    }

    private int resolveNextQueueNumber(LocalDate appointmentDate) {
        return appointmentRequestRepository.findByAppointmentDateOrderByAppointmentTimeAscCreatedAtAsc(appointmentDate).stream()
                .map(AppointmentRequest::getQueueNumber)
                .filter(item -> item != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
