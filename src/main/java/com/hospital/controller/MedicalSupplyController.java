package com.hospital.controller;

import com.hospital.entity.MedicalSupply;
import com.hospital.entity.SupplyBatch;
import com.hospital.service.MasterDataService;
import com.hospital.service.MedicalSupplyService;
import com.hospital.service.SupplyBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/medical-supplies")
@RequiredArgsConstructor
public class MedicalSupplyController {

    private final MedicalSupplyService medicalSupplyService;
    private final MasterDataService masterDataService;
    private final SupplyBatchService supplyBatchService;

    @GetMapping
    public String listSupplies(@RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {
        Page<MedicalSupply> supplyPage = medicalSupplyService.getAllSupplies(keyword, page, size);
        model.addAttribute("supplyPage", supplyPage);
        model.addAttribute("keyword", keyword);
        return "inventory/vat-tu-list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String createForm(Model model) {
        MedicalSupply medicalSupply = new MedicalSupply();
        medicalSupply.setImportDate(LocalDate.now());
        medicalSupply.setExpiryDate(LocalDate.now().plusMonths(12));
        populateMasterData(model);
        model.addAttribute("medicalSupply", medicalSupply);
        model.addAttribute("pageTitle", "Thêm vật tư y tế");
        return "inventory/vat-tu-form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String editForm(@PathVariable Long id, Model model) {
        populateMasterData(model);
        model.addAttribute("medicalSupply", medicalSupplyService.getById(id));
        model.addAttribute("pageTitle", "Cập nhật vật tư y tế");
        return "inventory/vat-tu-form";
    }

    @GetMapping("/{id}/batches")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String batchView(@PathVariable Long id, Model model) {
        MedicalSupply supply = medicalSupplyService.getById(id);
        List<SupplyBatch> batches = supplyBatchService.getBatchesBySupply(supply);
        model.addAttribute("supply", supply);
        model.addAttribute("batches", batches);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("warningDate", LocalDate.now().plusDays(30));
        return "inventory/batch-list";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public String saveSupply(@Valid @ModelAttribute("medicalSupply") MedicalSupply medicalSupply,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            populateMasterData(model);
            model.addAttribute("pageTitle", medicalSupply.getId() == null ? "Thêm vật tư y tế" : "Cập nhật vật tư y tế");
            return "inventory/vat-tu-form";
        }
        medicalSupplyService.save(medicalSupply);
        return "redirect:/medical-supplies?success=true";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSupply(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!medicalSupplyService.canDelete(id)) {
            redirectAttributes.addFlashAttribute("deleteError", medicalSupplyService.getDeleteRestrictionMessage(id));
            return "redirect:/medical-supplies";
        }
        medicalSupplyService.delete(id);
        redirectAttributes.addFlashAttribute("deleteSuccess", "Xóa vật tư thành công.");
        return "redirect:/medical-supplies?deleted=true";
    }

    private void populateMasterData(Model model) {
        model.addAttribute("categories", masterDataService.getAllCategories());
        model.addAttribute("suppliers", masterDataService.getAllSuppliers());
    }
}
