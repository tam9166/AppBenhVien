package com.hospital.controller;

import com.hospital.dto.OutboundReceiptForm;
import com.hospital.dto.ReceiptDetailForm;
import com.hospital.service.MedicalSupplyService;
import com.hospital.service.OutboundReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/outbound")
@RequiredArgsConstructor
public class OutboundReceiptController {

    private final OutboundReceiptService outboundReceiptService;
    private final MedicalSupplyService medicalSupplyService;

    @GetMapping("/receipts")
    public String listReceipts(Model model) {
        model.addAttribute("receipts", outboundReceiptService.getAllReceipts());
        model.addAttribute("receiptForm", buildDefaultForm());
        model.addAttribute("supplies", medicalSupplyService.getAllSupplies("", 0, 200).getContent());
        return "outbound/phieu-xuat";
    }

    @PostMapping("/receipts")
    public String createReceipt(@Valid @ModelAttribute("receiptForm") OutboundReceiptForm form,
                                BindingResult bindingResult,
                                Model model) {
        validateDetails(form, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("receipts", outboundReceiptService.getAllReceipts());
            model.addAttribute("supplies", medicalSupplyService.getAllSupplies("", 0, 200).getContent());
            return "outbound/phieu-xuat";
        }
        outboundReceiptService.createReceipt(form);
        return "redirect:/outbound/receipts?success=true";
    }

    private OutboundReceiptForm buildDefaultForm() {
        OutboundReceiptForm form = new OutboundReceiptForm();
        form.setReceiptCode("PX-" + System.currentTimeMillis());
        form.setReceiptDate(LocalDate.now());
        form.setCreatedBy("Nhan vien kho");
        form.setDepartmentName("Khoa cap cuu");
        form.getDetails().addAll(List.of(new ReceiptDetailForm(), new ReceiptDetailForm()));
        return form;
    }

    private void validateDetails(OutboundReceiptForm form, BindingResult bindingResult) {
        boolean hasValidDetail = false;

        for (int i = 0; i < form.getDetails().size(); i++) {
            ReceiptDetailForm detail = form.getDetails().get(i);
            if (detail == null || detail.isEmpty()) {
                continue;
            }

            if (!detail.hasCoreValues()) {
                bindingResult.addError(new FieldError("receiptForm", "details[" + i + "].medicalSupplyId",
                        "Moi dong xuat kho phai chon vat tu, so luong va don gia."));
                continue;
            }

            hasValidDetail = true;
        }

        if (!hasValidDetail) {
            bindingResult.rejectValue("details", "receiptForm.details",
                    "Phieu xuat phai co it nhat mot dong vat tu hop le.");
        }
    }
}
