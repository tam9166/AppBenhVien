package com.hospital.service.impl;

import com.hospital.dto.OutboundReceiptForm;
import com.hospital.dto.ReceiptDetailForm;
import com.hospital.entity.MedicalSupply;
import com.hospital.entity.OutboundReceipt;
import com.hospital.entity.OutboundReceiptDetail;
import com.hospital.entity.StockMovementType;
import com.hospital.exception.InsufficientStockException;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.OutboundReceiptRepository;
import com.hospital.service.AuditLogService;
import com.hospital.service.OutboundReceiptService;
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
public class OutboundReceiptServiceImpl implements OutboundReceiptService {

    private final OutboundReceiptRepository outboundReceiptRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyBatchService supplyBatchService;
    private final AuditLogService auditLogService;
    private final StockMovementService stockMovementService;

    @Override
    @Transactional
    public OutboundReceipt createReceipt(OutboundReceiptForm form) {
        OutboundReceipt receipt = new OutboundReceipt();
        receipt.setReceiptCode(form.getReceiptCode());
        receipt.setReceiptDate(form.getReceiptDate());
        receipt.setCreatedBy(form.getCreatedBy());
        receipt.setDepartmentName(form.getDepartmentName());
        receipt.setNote(form.getNote());

        List<OutboundReceiptDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ReceiptDetailForm detailForm : form.getDetails()) {
            if (detailForm.getMedicalSupplyId() == null || detailForm.getQuantity() == null || detailForm.getUnitPrice() == null) {
                continue;
            }

            MedicalSupply medicalSupply = medicalSupplyRepository.findById(detailForm.getMedicalSupplyId())
                    .orElseThrow(() -> new IllegalArgumentException("Vật tư không tồn tại"));
            int quantityBefore = medicalSupply.getQuantity() == null ? 0 : medicalSupply.getQuantity();

            if (medicalSupply.getQuantity() < detailForm.getQuantity()) {
                throw new InsufficientStockException("Vật tư " + medicalSupply.getName() + " không đủ số lượng để xuất.");
            }

            OutboundReceiptDetail detail = new OutboundReceiptDetail();
            detail.setReceipt(receipt);
            detail.setMedicalSupply(medicalSupply);
            detail.setQuantity(detailForm.getQuantity());
            detail.setUnitPrice(detailForm.getUnitPrice());
            detail.setAmount(detailForm.getUnitPrice().multiply(BigDecimal.valueOf(detailForm.getQuantity())));
            detail.setAllocatedBatches(supplyBatchService.allocateForOutbound(medicalSupply, detailForm.getQuantity()));
            details.add(detail);
            stockMovementService.logMovement(
                    medicalSupply.getCode(),
                    medicalSupply.getName(),
                    null,
                    StockMovementType.OUTBOUND,
                    -detailForm.getQuantity(),
                    quantityBefore,
                    quantityBefore - detailForm.getQuantity(),
                    form.getReceiptCode(),
                    "OUTBOUND_RECEIPT",
                    form.getCreatedBy(),
                    "Xuất kho theo phiếu " + form.getReceiptCode() + ". Gợi ý FEFO: " + detail.getAllocatedBatches(),
                    form.getReceiptDate());

            total = total.add(detail.getAmount());
        }

        receipt.setDetails(details);
        receipt.setTotalAmount(total);
        OutboundReceipt savedReceipt = outboundReceiptRepository.save(receipt);
        auditLogService.log(form.getCreatedBy(), "CREATE_OUTBOUND_RECEIPT", "OUTBOUND_RECEIPT", form.getReceiptCode(),
                "Tạo phiếu xuất " + form.getReceiptCode() + " cho khoa " + form.getDepartmentName() + ".");
        return savedReceipt;
    }

    @Override
    public List<OutboundReceipt> getAllReceipts() {
        return outboundReceiptRepository.findAll();
    }

    @Override
    public long countReceiptsByMonth(int year, int month) {
        return outboundReceiptRepository.countByMonth(year, month);
    }

    @Override
    public long countReceiptsByDate(LocalDate receiptDate) {
        return outboundReceiptRepository.countByReceiptDate(receiptDate);
    }
}
