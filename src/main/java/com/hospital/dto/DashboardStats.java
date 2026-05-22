package com.hospital.dto;

import com.hospital.entity.MedicalSupply;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardStats {
    private long totalSupplies;
    private long totalDrugs;
    private long expiringSoon;
    private long lowStock;
    private long activeAlerts;
    private long inboundReceiptCount;
    private long outboundReceiptCount;
    private long todayInboundReceiptCount;
    private long todayOutboundReceiptCount;
    private BigDecimal totalInventoryValue;
    private int selectedYear;
    private int selectedMonth;
    private String inboundChartData;
    private String outboundChartData;
    private String chartLabels;
    private String categoryChartLabels;
    private String categoryChartData;
    private List<DashboardAlertDto> recentAlerts;
    private List<AuditLogDto> recentAuditLogs;
    private List<MedicalSupply> topLowStockSupplies;
    private List<MedicalSupply> topExpiringSupplies;
    private List<TopUsedSupplyDto> topUsedSupplies;
}
