package com.hospital.service.impl;

import com.hospital.dto.InboundReceiptForm;
import com.hospital.dto.ReceiptDetailForm;
import com.hospital.entity.InboundReceipt;
import com.hospital.entity.InboundReceiptDetail;
import com.hospital.entity.MedicalSupply;
import com.hospital.entity.StockMovementType;
import com.hospital.repository.InboundReceiptRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.service.AuditLogService;
import com.hospital.service.InboundReceiptService;
import com.hospital.service.StockMovementService;
import com.hospital.service.SupplyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InboundReceiptServiceImpl implements InboundReceiptService {

    private final InboundReceiptRepository inboundReceiptRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyBatchService supplyBatchService;
    private final AuditLogService auditLogService;
    private final StockMovementService stockMovementService;

    @Override
    @Transactional
    public InboundReceipt createReceipt(InboundReceiptForm form) {
        InboundReceipt receipt = new InboundReceipt();
        receipt.setReceiptCode(form.getReceiptCode());
        receipt.setReceiptDate(form.getReceiptDate());
        receipt.setCreatedBy(form.getCreatedBy());
        receipt.setNote(form.getNote());

        List<InboundReceiptDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ReceiptDetailForm detailForm : form.getDetails()) {
            if (detailForm.getMedicalSupplyId() == null || detailForm.getQuantity() == null || detailForm.getUnitPrice() == null) {
                continue;
            }

            MedicalSupply medicalSupply = medicalSupplyRepository.findById(detailForm.getMedicalSupplyId())
                    .orElseThrow(() -> new IllegalArgumentException("Vật tư không tồn tại"));
            int quantityBefore = medicalSupply.getQuantity() == null ? 0 : medicalSupply.getQuantity();

            InboundReceiptDetail detail = new InboundReceiptDetail();
            detail.setReceipt(receipt);
            detail.setMedicalSupply(medicalSupply);
            detail.setQuantity(detailForm.getQuantity());
            detail.setUnitPrice(detailForm.getUnitPrice());
            detail.setAmount(detailForm.getUnitPrice().multiply(BigDecimal.valueOf(detailForm.getQuantity())));
            detail.setBatchNumber(detailForm.getBatchNumber());
            detail.setManufactureDate(detailForm.getManufactureDate());
            detail.setBatchExpiryDate(detailForm.getExpiryDate() != null ? detailForm.getExpiryDate() : medicalSupply.getExpiryDate());
            details.add(detail);

            if (detailForm.getUnitPrice() != null) {
                medicalSupply.setEstimatedUnitPrice(detailForm.getUnitPrice());
            }
            medicalSupplyRepository.save(medicalSupply);
            supplyBatchService.createBatch(
                    medicalSupply,
                    detailForm.getBatchNumber(),
                    detailForm.getManufactureDate(),
                    detail.getBatchExpiryDate(),
                    detailForm.getQuantity());
            stockMovementService.logMovement(
                    medicalSupply.getCode(),
                    medicalSupply.getName(),
                    detailForm.getBatchNumber(),
                    StockMovementType.INBOUND,
                    detailForm.getQuantity(),
                    quantityBefore,
                    quantityBefore + detailForm.getQuantity(),
                    form.getReceiptCode(),
                    "INBOUND_RECEIPT",
                    form.getCreatedBy(),
                    "Nhập kho theo phiếu " + form.getReceiptCode(),
                    form.getReceiptDate());
            total = total.add(detail.getAmount());
        }

        receipt.setDetails(details);
        receipt.setTotalAmount(total);
        InboundReceipt savedReceipt = inboundReceiptRepository.save(receipt);
        auditLogService.log(form.getCreatedBy(), "CREATE_INBOUND_RECEIPT", "INBOUND_RECEIPT", form.getReceiptCode(),
                "Tạo phiếu nhập " + form.getReceiptCode() + " với " + details.size() + " dòng vật tư.");
        return savedReceipt;
    }

    @Override
    public List<InboundReceipt> getAllReceipts() {
        return inboundReceiptRepository.findAll();
    }

    @Override
    public long countReceiptsByMonth(int year, int month) {
        return inboundReceiptRepository.countByMonth(year, month);
    }

    @Override
    public long countReceiptsByDate(LocalDate receiptDate) {
        return inboundReceiptRepository.countByReceiptDate(receiptDate);
    }
}
