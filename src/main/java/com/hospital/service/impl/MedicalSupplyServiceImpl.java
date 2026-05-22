package com.hospital.service.impl;

import com.hospital.entity.MedicalSupply;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InboundReceiptDetailRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.OutboundReceiptDetailRepository;
import com.hospital.repository.SupplyBatchRepository;
import com.hospital.service.AuditLogService;
import com.hospital.service.MedicalSupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalSupplyServiceImpl implements MedicalSupplyService {

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final InboundReceiptDetailRepository inboundReceiptDetailRepository;
    private final OutboundReceiptDetailRepository outboundReceiptDetailRepository;
    private final SupplyBatchRepository supplyBatchRepository;
    private final AuditLogService auditLogService;

    @Override
    public Page<MedicalSupply> getAllSupplies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (StringUtils.hasText(keyword)) {
            return medicalSupplyRepository.search(keyword.trim(), pageable);
        }
        return medicalSupplyRepository.findAll(pageable);
    }

    @Override
    public MedicalSupply getById(Long id) {
        return medicalSupplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư với id = " + id));
    }

    @Override
    public MedicalSupply getByCode(String code) {
        return medicalSupplyRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư với mã = " + code));
    }

    @Override
    public MedicalSupply getByQrCode(String qrCode) {
        return medicalSupplyRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư theo QR Code."));
    }

    @Override
    @Transactional
    public MedicalSupply save(MedicalSupply medicalSupply) {
        boolean isNew = medicalSupply.getId() == null;
        if (medicalSupply.getEstimatedUnitPrice() == null) {
            medicalSupply.setEstimatedUnitPrice(BigDecimal.ZERO);
        }
        if (!StringUtils.hasText(medicalSupply.getQrCode())) {
            medicalSupply.setQrCode("QR-" + UUID.randomUUID());
        }
        MedicalSupply savedSupply = medicalSupplyRepository.save(medicalSupply);
        auditLogService.log(
                getCurrentUsername(),
                isNew ? "CREATE_SUPPLY" : "UPDATE_SUPPLY",
                "MEDICAL_SUPPLY",
                savedSupply.getCode(),
                (isNew ? "Thêm mới" : "Cập nhật") + " vật tư " + savedSupply.getName() + "."
        );
        return savedSupply;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MedicalSupply medicalSupply = getById(id);
        if (!canDelete(id)) {
            throw new IllegalStateException(getDeleteRestrictionMessage(id));
        }
        medicalSupplyRepository.delete(medicalSupply);
        auditLogService.log(
                getCurrentUsername(),
                "DELETE_SUPPLY",
                "MEDICAL_SUPPLY",
                medicalSupply.getCode(),
                "Xóa vật tư " + medicalSupply.getName() + "."
        );
    }

    @Override
    public boolean canDelete(Long id) {
        return !inboundReceiptDetailRepository.existsByMedicalSupply_Id(id)
                && !outboundReceiptDetailRepository.existsByMedicalSupply_Id(id)
                && !supplyBatchRepository.existsByMedicalSupply_Id(id);
    }

    @Override
    public String getDeleteRestrictionMessage(Long id) {
        MedicalSupply medicalSupply = getById(id);
        return "Không thể xóa vật tư " + medicalSupply.getName()
                + " vì đã phát sinh nhập kho, xuất kho hoặc lô hàng liên quan. "
                + "Chỉ các vật tư mới tạo, chưa được sử dụng hoặc lên đơn mới được phép xóa.";
    }

    @Override
    public List<MedicalSupply> getExpiringSupplies(LocalDate threshold) {
        return medicalSupplyRepository.findExpiringSupplies(threshold);
    }

    @Override
    public List<MedicalSupply> getLowStockSupplies() {
        return medicalSupplyRepository.findLowStockSupplies();
    }

    @Override
    public long countAll() {
        return medicalSupplyRepository.count();
    }

    @Override
    public long countDrugs() {
        return medicalSupplyRepository.countByCategory_NameContainingIgnoreCase("thuốc");
    }

    @Override
    public long countExpiringSoon() {
        return medicalSupplyRepository.countByExpiryDateBefore(LocalDate.now().plusDays(30));
    }

    @Override
    public long countLowStock() {
        return medicalSupplyRepository.findLowStockSupplies().size();
    }

    @Override
    public BigDecimal calculateTotalInventoryValue() {
        return medicalSupplyRepository.findAll().stream()
                .map(supply -> {
                    BigDecimal unitPrice = supply.getEstimatedUnitPrice() != null
                            ? supply.getEstimatedUnitPrice()
                            : BigDecimal.ZERO;
                    Integer quantity = supply.getQuantity() != null ? supply.getQuantity() : 0;
                    return unitPrice.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, Long> getCategoryDistribution() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : medicalSupplyRepository.getCategoryDistribution()) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
