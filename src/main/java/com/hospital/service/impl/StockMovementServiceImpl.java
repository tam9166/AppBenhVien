package com.hospital.service.impl;

import com.hospital.entity.StockMovement;
import com.hospital.entity.StockMovementType;
import com.hospital.repository.StockMovementRepository;
import com.hospital.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public void logMovement(String supplyCode, String supplyName, String batchNumber, StockMovementType movementType,
                            int quantityChange, int quantityBeforeChange, int quantityAfterChange, String referenceCode,
                            String referenceType, String actor, String note, LocalDate movementDate) {
        StockMovement movement = new StockMovement();
        movement.setSupplyCode(supplyCode);
        movement.setSupplyName(supplyName);
        movement.setBatchNumber(batchNumber);
        movement.setMovementType(movementType);
        movement.setQuantityChange(quantityChange);
        movement.setQuantityBeforeChange(quantityBeforeChange);
        movement.setQuantityAfterChange(quantityAfterChange);
        movement.setReferenceCode(referenceCode);
        movement.setReferenceType(referenceType);
        movement.setActor(actor);
        movement.setNote(note);
        movement.setMovementDate(movementDate);
        stockMovementRepository.save(movement);
    }

    @Override
    public List<StockMovement> getRecentMovements() {
        return stockMovementRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    public List<StockMovement> getAllMovements() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Object[]> summarizeUsageFromDate(StockMovementType movementType, LocalDate fromDate) {
        return stockMovementRepository.summarizeUsageFromDate(movementType, fromDate);
    }

    @Override
    public List<Object[]> summarizeOutboundByDate(LocalDate movementDate) {
        return stockMovementRepository.summarizeOutboundByDate(movementDate);
    }
}
