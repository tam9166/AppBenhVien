package com.hospital.service.impl;

import com.hospital.entity.MedicalSupply;
import com.hospital.entity.SupplyBatch;
import com.hospital.exception.InsufficientStockException;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.SupplyBatchRepository;
import com.hospital.service.SupplyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyBatchServiceImpl implements SupplyBatchService {

    private final SupplyBatchRepository supplyBatchRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;

    @Override
    @Transactional
    public SupplyBatch createBatch(MedicalSupply medicalSupply, String batchNumber, LocalDate manufactureDate, LocalDate expiryDate, Integer quantity) {
        SupplyBatch batch = new SupplyBatch();
        batch.setMedicalSupply(medicalSupply);
        batch.setBatchNumber(batchNumber);
        batch.setManufactureDate(manufactureDate);
        batch.setExpiryDate(expiryDate);
        batch.setQuantity(quantity);
        SupplyBatch savedBatch = supplyBatchRepository.save(batch);
        syncSupplyQuantity(medicalSupply);
        return savedBatch;
    }

    @Override
    public List<SupplyBatch> getBatchesBySupply(MedicalSupply medicalSupply) {
        return supplyBatchRepository.findByMedicalSupplyOrderByExpiryDateAscBatchNumberAsc(medicalSupply);
    }

    @Override
    public List<SupplyBatch> getExpiringBatches(LocalDate threshold) {
        return supplyBatchRepository.findExpiringBatches(threshold);
    }

    @Override
    @Transactional
    public String allocateForOutbound(MedicalSupply medicalSupply, Integer quantity) {
        int remaining = quantity;
        List<String> allocations = new ArrayList<>();
        List<SupplyBatch> batches = getBatchesBySupply(medicalSupply);

        for (SupplyBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            if (batch.getQuantity() <= 0) {
                continue;
            }
            int used = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - used);
            remaining -= used;
            allocations.add(batch.getBatchNumber() + " x " + used);
        }

        if (remaining > 0) {
            throw new InsufficientStockException("Không đủ tồn theo lô cho vật tư " + medicalSupply.getName());
        }

        supplyBatchRepository.saveAll(batches);
        syncSupplyQuantity(medicalSupply);
        return String.join(", ", allocations);
    }

    @Override
    @Transactional
    public void syncSupplyQuantity(MedicalSupply medicalSupply) {
        Integer total = supplyBatchRepository.sumQuantityBySupplyId(medicalSupply.getId());
        medicalSupply.setQuantity(total == null ? 0 : total);
        medicalSupplyRepository.save(medicalSupply);
    }
}
