package com.hospital.service.impl;

import com.hospital.dto.AuditLogDto;
import com.hospital.dto.DashboardAlertDto;
import com.hospital.dto.DashboardStats;
import com.hospital.service.AlertService;
import com.hospital.service.AuditLogService;
import com.hospital.service.DashboardService;
import com.hospital.service.InboundReceiptService;
import com.hospital.service.MedicalSupplyService;
import com.hospital.service.OutboundReceiptService;
import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MedicalSupplyService medicalSupplyService;
    private final ReportService reportService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;
    private final InboundReceiptService inboundReceiptService;
    private final OutboundReceiptService outboundReceiptService;

    @Override
    public DashboardStats buildDashboardStats(int year, int month) {
        Map<Integer, Double> inboundMap = reportService.getInboundStatisticsByMonth(year);
        Map<Integer, Double> outboundMap = reportService.getOutboundStatisticsByMonth(year);
        Map<String, Long> categoryDistribution = medicalSupplyService.getCategoryDistribution();
        List<DashboardAlertDto> recentAlerts = alertService.getRecentActiveAlerts();
        List<AuditLogDto> recentLogs = auditLogService.getRecentLogs();
        LocalDate today = LocalDate.now();

        List<String> labels = new ArrayList<>();
        List<Double> inbound = new ArrayList<>();
        List<Double> outbound = new ArrayList<>();
        List<String> categoryLabels = new ArrayList<>();
        List<Long> categoryData = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            labels.add("\"T" + i + "\"");
            inbound.add(inboundMap.getOrDefault(i, 0.0));
            outbound.add(outboundMap.getOrDefault(i, 0.0));
        }
        categoryDistribution.forEach((key, value) -> {
            categoryLabels.add("\"" + key + "\"");
            categoryData.add(value);
        });

        return DashboardStats.builder()
                .totalSupplies(medicalSupplyService.countAll())
                .totalDrugs(medicalSupplyService.countDrugs())
                .expiringSoon(medicalSupplyService.countExpiringSoon())
                .lowStock(medicalSupplyService.countLowStock())
                .activeAlerts(alertService.countActiveAlerts())
                .inboundReceiptCount(inboundReceiptService.countReceiptsByMonth(year, month))
                .outboundReceiptCount(outboundReceiptService.countReceiptsByMonth(year, month))
                .todayInboundReceiptCount(inboundReceiptService.countReceiptsByDate(today))
                .todayOutboundReceiptCount(outboundReceiptService.countReceiptsByDate(today))
                .totalInventoryValue(medicalSupplyService.calculateTotalInventoryValue())
                .selectedYear(year)
                .selectedMonth(month)
                .chartLabels("[" + String.join(",", labels) + "]")
                .inboundChartData(inbound.toString())
                .outboundChartData(outbound.toString())
                .categoryChartLabels("[" + String.join(",", categoryLabels) + "]")
                .categoryChartData(categoryData.toString())
                .recentAlerts(recentAlerts)
                .recentAuditLogs(recentLogs)
                .topLowStockSupplies(medicalSupplyService.getLowStockSupplies().stream().limit(5).toList())
                .topExpiringSupplies(medicalSupplyService.getExpiringSupplies(today.plusDays(30)).stream().limit(5).toList())
                .topUsedSupplies(reportService.getTopUsedSupplies(30))
                .build();
    }
}
