package com.hospital.service;

import com.hospital.entity.MedicalSupply;
import com.hospital.entity.SupplyBatch;

import java.time.LocalDate;
import java.util.List;

public interface SupplyBatchService {
    SupplyBatch createBatch(MedicalSupply medicalSupply, String batchNumber, LocalDate manufactureDate, LocalDate expiryDate, Integer quantity);

    List<SupplyBatch> getBatchesBySupply(MedicalSupply medicalSupply);

    List<SupplyBatch> getExpiringBatches(LocalDate threshold);

    String allocateForOutbound(MedicalSupply medicalSupply, Integer quantity);

    void syncSupplyQuantity(MedicalSupply medicalSupply);
}
