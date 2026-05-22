package com.hospital.controller;

import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

/**
 * Controller báo cáo và xuất file.
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String reportPage(Model model) {
        int year = LocalDate.now().getYear();
        model.addAttribute("year", year);
        model.addAttribute("inboundStats", reportService.getInboundStatisticsByMonth(year));
        model.addAttribute("outboundStats", reportService.getOutboundStatisticsByMonth(year));
        model.addAttribute("topUsedSupplies", reportService.getTopUsedSupplies());
        model.addAttribute("recentStockMovements", reportService.getRecentStockMovements());
        return "report/report";
    }

    @GetMapping("/excel")
    public ResponseEntity<ByteArrayResource> exportExcel() {
        byte[] bytes = reportService.exportSuppliesToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao-cao-vat-tu.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/pdf")
    public ResponseEntity<ByteArrayResource> exportPdf() {
        byte[] bytes = reportService.exportSummaryToPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao-cao-vat-tu.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/stock-movements/excel")
    public ResponseEntity<ByteArrayResource> exportStockMovementExcel() {
        byte[] bytes = reportService.exportStockMovementsToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao-cao-bien-dong-kho.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/stock-movements/pdf")
    public ResponseEntity<ByteArrayResource> exportStockMovementPdf() {
        byte[] bytes = reportService.exportStockMovementsToPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bao-cao-bien-dong-kho.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }
}
