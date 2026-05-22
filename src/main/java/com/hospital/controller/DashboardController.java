package com.hospital.controller;

import com.hospital.entity.AlertType;
import com.hospital.entity.AppointmentStatus;
import com.hospital.repository.AlertNotificationRepository;
import com.hospital.repository.AppointmentRequestRepository;
import com.hospital.repository.ChatbotConversationLogRepository;
import com.hospital.repository.DepartmentSupplyRecommendationRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.OutboundReceiptDetailRepository;
import com.hospital.repository.ScreeningTicketRepository;
import com.hospital.service.DashboardService;
import com.hospital.service.MedicalSupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final MedicalSupplyService medicalSupplyService;
    private final ChatbotConversationLogRepository chatbotConversationLogRepository;
    private final AppointmentRequestRepository appointmentRequestRepository;
    private final ScreeningTicketRepository screeningTicketRepository;
    private final DepartmentSupplyRecommendationRepository departmentSupplyRecommendationRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final OutboundReceiptDetailRepository outboundReceiptDetailRepository;
    private final AlertNotificationRepository alertNotificationRepository;

    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            Model model) {
        LocalDate now = LocalDate.now();
        int selectedYear = year != null ? year : now.getYear();
        int selectedMonth = month != null ? month : now.getMonthValue();

        model.addAttribute("stats", dashboardService.buildDashboardStats(selectedYear, selectedMonth));
        model.addAttribute("expiringSupplies", medicalSupplyService.getExpiringSupplies(LocalDate.now().plusDays(30)));
        model.addAttribute("lowStockSupplies", medicalSupplyService.getLowStockSupplies());

        model.addAttribute("chatbotTotal", chatbotConversationLogRepository.count());
        model.addAttribute("chatbotEmergencyCount", chatbotConversationLogRepository.findAll().stream().filter(log -> log.isEmergency()).count());
        model.addAttribute("chatbotDepartmentStats", buildChatbotDepartmentStats());

        model.addAttribute("appointmentsToday", appointmentRequestRepository.countByAppointmentDate(LocalDate.now()));
        model.addAttribute("pendingAppointments", appointmentRequestRepository.countByStatus(AppointmentStatus.PENDING));
        model.addAttribute("recentAppointments", appointmentRequestRepository.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute("screeningEmergencyCount", screeningTicketRepository.countByEmergencyTrue());
        model.addAttribute("recentScreeningTickets", screeningTicketRepository.findTop10ByOrderByCreatedAtDesc());

        model.addAttribute("appointmentNotificationCount",
                alertNotificationRepository.countByAlertTypeAndResolvedFalse(AlertType.APPOINTMENT_REQUEST));
        model.addAttribute("recentAppointmentNotifications",
                alertNotificationRepository.findTop10ByAlertTypeAndResolvedFalseOrderByLastDetectedAtDesc(AlertType.APPOINTMENT_REQUEST));

        model.addAttribute("supplyForecasts", buildSupplyForecasts());
        model.addAttribute("expiryCalendar", medicalSupplyRepository.findAll().stream()
                .filter(item -> item.getExpiryDate() != null)
                .sorted(Comparator.comparing(item -> item.getExpiryDate()))
                .limit(8)
                .toList());
        model.addAttribute("departmentSupplyRecommendations", departmentSupplyRecommendationRepository.findAll());
        model.addAttribute("yearOptions", IntStream.rangeClosed(now.getYear() - 3, now.getYear() + 1).boxed().toList());
        model.addAttribute("monthOptions", IntStream.rangeClosed(1, 12).boxed().toList());
        return "dashboard/dashboard";
    }

    private Map<String, Long> buildChatbotDepartmentStats() {
        Map<String, Long> result = new LinkedHashMap<>();
        chatbotConversationLogRepository.findAll().stream()
                .filter(log -> log.getDepartment() != null && !log.getDepartment().isBlank())
                .forEach(log -> result.merge(log.getDepartment(), 1L, Long::sum));
        return result;
    }

    private List<Map<String, Object>> buildSupplyForecasts() {
        Map<String, Long> usedByName = new LinkedHashMap<>();
        for (Object[] row : outboundReceiptDetailRepository.findTopUsedSupplies()) {
            usedByName.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return medicalSupplyRepository.findAll().stream()
                .filter(item -> item.getQuantity() != null)
                .sorted(Comparator.comparing(item -> item.getQuantity()))
                .limit(8)
                .map(item -> {
                    long used = usedByName.getOrDefault(item.getName(), 0L);
                    long dailyAverage = Math.max(1L, Math.round(used / 30.0));
                    long remainingDays = item.getQuantity() / dailyAverage;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", item.getCode());
                    row.put("name", item.getName());
                    row.put("quantity", item.getQuantity());
                    row.put("remainingDays", remainingDays);
                    row.put("dailyAverage", dailyAverage);
                    return row;
                })
                .toList();
    }
}
