package com.hospital.service;

import com.hospital.dto.TopUsedSupplyDto;
import com.hospital.entity.StockMovement;

import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<Integer, Double> getInboundStatisticsByMonth(int year);

    Map<Integer, Double> getOutboundStatisticsByMonth(int year);

    List<TopUsedSupplyDto> getTopUsedSupplies();

    List<TopUsedSupplyDto> getTopUsedSupplies(int days);

    List<StockMovement> getRecentStockMovements();

    byte[] exportSuppliesToExcel();

    byte[] exportSummaryToPdf();

    byte[] exportStockMovementsToExcel();

    byte[] exportStockMovementsToPdf();
}
