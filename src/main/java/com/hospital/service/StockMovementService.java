package com.hospital.service;

import com.hospital.entity.StockMovement;
import com.hospital.entity.StockMovementType;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementService {

    void logMovement(String supplyCode, String supplyName, String batchNumber, StockMovementType movementType,
                     int quantityChange, int quantityBeforeChange, int quantityAfterChange, String referenceCode,
                     String referenceType, String actor, String note, LocalDate movementDate);

    List<StockMovement> getRecentMovements();

    List<StockMovement> getAllMovements();

    List<Object[]> summarizeUsageFromDate(StockMovementType movementType, LocalDate fromDate);

    List<Object[]> summarizeOutboundByDate(LocalDate movementDate);
}
