package com.hospital.controller;

import com.hospital.dto.AssistantResponse;
import com.hospital.entity.AppointmentRequest;
import com.hospital.entity.AuditLog;
import com.hospital.entity.ScreeningTicket;
import com.hospital.repository.AppointmentRequestRepository;
import com.hospital.repository.AuditLogRepository;
import com.hospital.repository.CancerScreeningPackageRepository;
import com.hospital.repository.DepartmentInfoRepository;
import com.hospital.repository.MedicalServicePriceRepository;
import com.hospital.repository.ScreeningTicketRepository;
import com.hospital.service.AlertService;
import com.hospital.service.HospitalAssistantService;
import com.hospital.utils.QrCodeGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PublicPortalController {
    private static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    private final AppointmentRequestRepository appointmentRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final DepartmentInfoRepository departmentInfoRepository;
    private final MedicalServicePriceRepository medicalServicePriceRepository;
    private final CancerScreeningPackageRepository cancerScreeningPackageRepository;
    private final ScreeningTicketRepository screeningTicketRepository;
    private final HospitalAssistantService hospitalAssistantService;
    private final AlertService alertService;
    private final QrCodeGenerator qrCodeGenerator;

    @GetMapping
    public String portal(Model model) {
        model.addAttribute("departments", departmentInfoRepository.findAll());
        model.addAttribute("prices", medicalServicePriceRepository.findAll());
        model.addAttribute("packages", cancerScreeningPackageRepository.findAll());
        model.addAttribute("appointment", new AppointmentRequest());
        return "patient/portal";
    }

    @GetMapping("/appointments/new")
    public String appointmentForm(@RequestParam(required = false) Long screeningTicketId, Model model) {
        if (!model.containsAttribute("appointment")) {
            AppointmentRequest appointment = new AppointmentRequest();
            if (screeningTicketId != null) {
                screeningTicketRepository.findById(screeningTicketId).ifPresent(ticket -> prefillFromTicket(appointment, ticket));
            }
            repairAppointmentText(appointment);
        model.addAttribute("appointment", appointment);
        }
        model.addAttribute("departments", departmentInfoRepository.findAll());
        model.addAttribute("screeningTicketId", screeningTicketId);
        return "patient/appointment-form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@Valid @ModelAttribute("appointment") AppointmentRequest appointment,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentInfoRepository.findAll());
            return "patient/appointment-form";
        }

        enrichAppointmentFromSymptoms(appointment);
        AppointmentRequest saved = appointmentRequestRepository.save(appointment);
        alertService.createAppointmentRequestAlert(saved);
        auditLogRepository.save(buildAppointmentAuditLog(saved));

        redirectAttributes.addFlashAttribute("successMessage",
                "Äáº·t lá»‹ch thÃ nh cÃ´ng. Vui lÃ²ng lÆ°u mÃ£ QR Ä‘á»ƒ check-in.");
        return "redirect:/patient/appointments/" + saved.getId();
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetail(@PathVariable Long id, Model model) {
        AppointmentRequest appointment = appointmentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y lá»‹ch háº¹n"));
        repairAppointmentText(appointment);
        model.addAttribute("appointment", appointment);
        if (appointment.getScreeningTicketId() != null) {
            screeningTicketRepository.findById(appointment.getScreeningTicketId())
                    .ifPresent(ticket -> model.addAttribute("screeningTicket", ticket));
        }
        return "patient/appointment-detail";
    }

    @GetMapping(value = "/appointments/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> appointmentQr(@PathVariable Long id) {
        AppointmentRequest appointment = appointmentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y lá»‹ch háº¹n"));
        byte[] image = qrCodeGenerator.generateQrCode("/patient/check-in/" + appointment.getQrCode(), 260, 260);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
    }

    @GetMapping("/appointments/lookup")
    public String lookupAppointments(@RequestParam(defaultValue = "") String phone, Model model) {
        List<AppointmentRequest> appointments = phone.isBlank()
                ? List.of()
                : appointmentRequestRepository.findByPhoneOrderByCreatedAtDesc(phone.trim());
        model.addAttribute("lookupPhone", phone);
        model.addAttribute("appointments", appointments);
        return "patient/appointment-lookup";
    }

    @GetMapping("/check-in/{qrCode}")
    public String checkIn(@PathVariable String qrCode, Model model) {
        AppointmentRequest appointment = appointmentRequestRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("MÃ£ QR lá»‹ch háº¹n khÃ´ng há»£p lá»‡"));
        repairAppointmentText(appointment);
        model.addAttribute("appointment", appointment);
        return "patient/check-in";
    }

    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentInfoRepository.findAll());
        return "patient/departments";
    }

    @GetMapping("/prices")
    public String prices(Model model) {
        model.addAttribute("prices", medicalServicePriceRepository.findAll());
        return "patient/prices";
    }

    @GetMapping("/screening-packages")
    public String screeningPackages(Model model) {
        model.addAttribute("packages", cancerScreeningPackageRepository.findAll());
        return "patient/screening-packages";
    }

    @PostMapping("/screening-ticket")
    public String createScreeningTicket(@RequestParam String message, RedirectAttributes redirectAttributes) {
        if (message == null || message.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("successMessage", "Vui l\u00f2ng nh\u1eadp tri\u1ec7u ch\u1ee9ng tr\u01b0\u1edbc khi t\u1ea1o phi\u1ebfu s\u00e0ng l\u1ecdc.");
            return "redirect:/patient";
        }
        AssistantResponse response = hospitalAssistantService.analyze(message.trim(), false);
        ScreeningTicket ticket = new ScreeningTicket();
        ticket.setPatientMessage(message.trim());
        ticket.setSuggestedDepartment(response.getDepartment() == null || response.getDepartment().isBlank()
                ? "N\u1ed9i t\u1ed5ng qu\u00e1t"
                : repairUtf8Deep(response.getDepartment()));
        ticket.setRiskScore(response.getRiskScore());
        ticket.setRiskLevel(repairUtf8Deep(response.getRiskLevel()));
        ticket.setEmergency(response.isEmergency());
        ticket.setSummary(repairUtf8Deep(response.getAnswer() + "\n" + response.getAdvice()));
        ScreeningTicket saved = screeningTicketRepository.save(ticket);
        redirectAttributes.addFlashAttribute("successMessage", "\u0110\u00e3 t\u1ea1o phi\u1ebfu s\u00e0ng l\u1ecdc t\u1ef1 \u0111\u1ed9ng.");
        return "redirect:/patient/screening-ticket/" + saved.getId();
    }

    @GetMapping("/screening-ticket/{id}")
    public String screeningTicket(@PathVariable Long id, Model model) {
        ScreeningTicket ticket = screeningTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y phiáº¿u sÃ ng lá»c"));
        repairScreeningTicketText(ticket);
        model.addAttribute("ticket", ticket);
        return "patient/screening-ticket";
    }

    private void prefillFromTicket(AppointmentRequest appointment, ScreeningTicket ticket) {
        repairScreeningTicketText(ticket);
        appointment.setScreeningTicketId(ticket.getId());
        appointment.setDepartment(ticket.getSuggestedDepartment());
        appointment.setSymptoms(ticket.getPatientMessage());
        appointment.setPriorityLevel(ticket.getRiskLevel());
        appointment.setEmergency(ticket.isEmergency());
        appointment.setTriageSummary(ticket.getSummary());
    }

    private void enrichAppointmentFromSymptoms(AppointmentRequest appointment) {
        if (appointment.getScreeningTicketId() != null && appointment.getTriageSummary() != null && !appointment.getTriageSummary().isBlank()) {
            return;
        }
        if (appointment.getSymptoms() == null || appointment.getSymptoms().isBlank()) {
            return;
        }
        AssistantResponse response = hospitalAssistantService.analyze(appointment.getSymptoms(), false);
        appointment.setPriorityLevel(response.getRiskLevel());
        appointment.setEmergency(response.isEmergency());
        appointment.setTriageSummary(response.getAnswer() + "\n" + response.getAdvice());
        if (appointment.getDepartment() == null || appointment.getDepartment().isBlank()) {
            appointment.setDepartment(response.getDepartment());
        }
    }

    private AuditLog buildAppointmentAuditLog(AppointmentRequest appointment) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername("patient-portal");
        auditLog.setAction("PATIENT_APPOINTMENT_CREATED");
        auditLog.setTargetType("APPOINTMENT");
        auditLog.setTargetValue(appointment.getQrCode());
        auditLog.setDescription("Bá»‡nh nhÃ¢n " + appointment.getPatientName()
                + " Ä‘Äƒng kÃ½ lá»‹ch khÃ¡m khoa " + appointment.getDepartment()
                + " vÃ o " + appointment.getAppointmentDate().format(DateTimeFormatter.ISO_DATE)
                + " " + appointment.getAppointmentTime() + ".");
        return auditLog;
    }

    private void repairAppointmentText(AppointmentRequest appointment) {
        appointment.setDepartment(repairUtf8Deep(appointment.getDepartment()));
        appointment.setPriorityLevel(repairUtf8Deep(appointment.getPriorityLevel()));
        appointment.setTriageSummary(repairUtf8Deep(appointment.getTriageSummary()));
    }

    private void repairScreeningTicketText(ScreeningTicket ticket) {
        if ((ticket.getPatientMessage() == null || ticket.getPatientMessage().isBlank())
                && ticket.getSummary() != null
                && ticket.getSummary().contains("?")) {
            ticket.setSuggestedDepartment("N\u1ed9i t\u1ed5ng qu\u00e1t");
            ticket.setRiskLevel("nh\u1eb9");
            ticket.setSummary("Vui l\u00f2ng nh\u1eadp c\u00e2u h\u1ecfi ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 t\u00f4i h\u1ed7 tr\u1ee3.\n"
                    + "B\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi v\u1ec1 gi\u1edd kh\u00e1m, \u0111\u1ecba ch\u1ec9, hotline ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 \u0111\u01b0\u1ee3c s\u00e0ng l\u1ecdc s\u01a1 b\u1ed9.");
            return;
        }
        ticket.setSuggestedDepartment(repairUtf8Deep(ticket.getSuggestedDepartment()));
        ticket.setRiskLevel(repairUtf8Deep(ticket.getRiskLevel()));
        ticket.setSummary(repairUtf8Deep(ticket.getSummary()));
    }

    private String repairUtf8Deep(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String repaired = value;
        for (int i = 0; i < 3 && looksMojibake(repaired); i++) {
            try {
                repaired = new String(repaired.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
            } catch (RuntimeException ex) {
                break;
            }
        }
        return repairReplacementArtifacts(repaired);
    }

    private boolean looksMojibake(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("Ä")
                || value.contains("áº")
                || value.contains("á»")
                || value.contains("Æ");
    }

    private String repairReplacementArtifacts(String value) {
        return value
                .replace("nh�m", "nhóm")
                .replace("b�?nh", "bệnh")
                .replace("hi�?n", "hiện")
                .replace("hi�?u", "hiệu")
                .replace("c�?p", "cấp")
                .replace("c�?u", "cứu")
                .replace("d�?u", "dấu")
                .replace("n�i", "nói")
                .replace("kh�", "khó")
                .replace("thay th�", "thay thế")
                .replace("ch�?n", "chẩn")
                .replace("đi�?u", "điều");
    }
}

