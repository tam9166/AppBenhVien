package com.hospital.controller;

import com.hospital.dto.InboundReceiptForm;
import com.hospital.dto.ReceiptDetailForm;
import com.hospital.service.InboundReceiptService;
import com.hospital.service.MedicalSupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/inbound")
@RequiredArgsConstructor
public class InboundReceiptController {

    private final InboundReceiptService inboundReceiptService;
    private final MedicalSupplyService medicalSupplyService;

    @GetMapping("/receipts")
    public String listReceipts(Model model) {
        model.addAttribute("receipts", inboundReceiptService.getAllReceipts());
        model.addAttribute("receiptForm", buildDefaultForm());
        model.addAttribute("supplies", medicalSupplyService.getAllSupplies("", 0, 200).getContent());
        return "inbound/phieu-nhap";
    }

    @PostMapping("/receipts")
    public String createReceipt(@Valid @ModelAttribute("receiptForm") InboundReceiptForm form,
                                BindingResult bindingResult,
                                Model model) {
        validateDetails(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("receipts", inboundReceiptService.getAllReceipts());
            model.addAttribute("supplies", medicalSupplyService.getAllSupplies("", 0, 200).getContent());
            return "inbound/phieu-nhap";
        }
        inboundReceiptService.createReceipt(form);
        return "redirect:/inbound/receipts?success=true";
    }

    private InboundReceiptForm buildDefaultForm() {
        InboundReceiptForm form = new InboundReceiptForm();
        form.setReceiptCode("PN-" + System.currentTimeMillis());
        form.setReceiptDate(LocalDate.now());
        form.setCreatedBy("Nhan vien kho");
        ReceiptDetailForm first = new ReceiptDetailForm();
        first.setBatchNumber("LO-" + System.currentTimeMillis());
        first.setExpiryDate(LocalDate.now().plusMonths(12));
        ReceiptDetailForm second = new ReceiptDetailForm();
        second.setBatchNumber("LO-" + (System.currentTimeMillis() + 1));
        second.setExpiryDate(LocalDate.now().plusMonths(12));
        form.getDetails().addAll(List.of(first, second));
        return form;
    }

    private void validateDetails(InboundReceiptForm form, BindingResult bindingResult) {
        boolean hasValidDetail = false;

        for (int i = 0; i < form.getDetails().size(); i++) {
            ReceiptDetailForm detail = form.getDetails().get(i);
            if (detail == null || detail.isEmpty()) {
                continue;
            }

            if (!detail.hasCoreValues()) {
                bindingResult.addError(new FieldError("receiptForm", "details[" + i + "].medicalSupplyId",
                        "Moi dong nhap kho phai chon vat tu, so luong va don gia."));
                continue;
            }

            if (!StringUtils.hasText(detail.getBatchNumber())) {
                bindingResult.addError(new FieldError("receiptForm", "details[" + i + "].batchNumber",
                        "So lo khong duoc de trong khi nhap kho."));
                continue;
            }

            hasValidDetail = true;
        }

        if (!hasValidDetail) {
            bindingResult.rejectValue("details", "receiptForm.details",
                    "Phieu nhap phai co it nhat mot dong vat tu hop le.");
        }
    }
}
