package com.hospital.service;

import com.hospital.entity.MedicalSupply;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MedicalSupplyService {
    Page<MedicalSupply> getAllSupplies(String keyword, int page, int size);

    MedicalSupply getById(Long id);

    MedicalSupply getByCode(String code);

    MedicalSupply getByQrCode(String qrCode);

    MedicalSupply save(MedicalSupply medicalSupply);

    void delete(Long id);

    boolean canDelete(Long id);

    String getDeleteRestrictionMessage(Long id);

    List<MedicalSupply> getExpiringSupplies(LocalDate threshold);

    List<MedicalSupply> getLowStockSupplies();

    long countAll();

    long countDrugs();

    long countExpiringSoon();

    long countLowStock();

    BigDecimal calculateTotalInventoryValue();

    Map<String, Long> getCategoryDistribution();
}
