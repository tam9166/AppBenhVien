package com.hospital.service.impl;

import com.hospital.dto.TopUsedSupplyDto;
import com.hospital.entity.MedicalSupply;
import com.hospital.entity.StockMovement;
import com.hospital.entity.StockMovementType;
import com.hospital.repository.InboundReceiptRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.OutboundReceiptDetailRepository;
import com.hospital.repository.OutboundReceiptRepository;
import com.hospital.repository.StockMovementRepository;
import com.hospital.service.ReportService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InboundReceiptRepository inboundReceiptRepository;
    private final OutboundReceiptRepository outboundReceiptRepository;
    private final OutboundReceiptDetailRepository outboundReceiptDetailRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public Map<Integer, Double> getInboundStatisticsByMonth(int year) {
        return convertToMonthMap(inboundReceiptRepository.getMonthlyInboundStatistics(year));
    }

    @Override
    public Map<Integer, Double> getOutboundStatisticsByMonth(int year) {
        return convertToMonthMap(outboundReceiptRepository.getMonthlyOutboundStatistics(year));
    }

    @Override
    public List<TopUsedSupplyDto> getTopUsedSupplies() {
        return outboundReceiptDetailRepository.findTopUsedSupplies().stream()
                .limit(5)
                .map(row -> new TopUsedSupplyDto(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public List<TopUsedSupplyDto> getTopUsedSupplies(int days) {
        LocalDate fromDate = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        return stockMovementRepository.summarizeUsageFromDate(StockMovementType.OUTBOUND, fromDate).stream()
                .limit(5)
                .map(row -> new TopUsedSupplyDto(String.valueOf(row[1]), ((Number) row[2]).longValue()))
                .toList();
    }

    @Override
    public List<StockMovement> getRecentStockMovements() {
        return stockMovementRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    public byte[] exportSuppliesToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("VatTu");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Ma vat tu");
            header.createCell(1).setCellValue("Ten vat tu");
            header.createCell(2).setCellValue("Loai");
            header.createCell(3).setCellValue("So luong");
            header.createCell(4).setCellValue("Don vi");
            header.createCell(5).setCellValue("Han su dung");
            header.createCell(6).setCellValue("Trang thai");

            int rowIdx = 1;
            for (MedicalSupply supply : medicalSupplyRepository.findAll()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(supply.getCode());
                row.createCell(1).setCellValue(supply.getName());
                row.createCell(2).setCellValue(supply.getCategory().getName());
                row.createCell(3).setCellValue(supply.getQuantity());
                row.createCell(4).setCellValue(supply.getUnit());
                row.createCell(5).setCellValue(String.valueOf(supply.getExpiryDate()));
                row.createCell(6).setCellValue(String.valueOf(supply.getStatus()));
            }

            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the xuat file Excel", ex);
        }
    }

    @Override
    public byte[] exportSummaryToPdf() {
        Document document = new Document();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("BAO CAO VAT TU Y TE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Ngay lap: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addCell(table, "Ma vat tu");
            addCell(table, "Ten vat tu");
            addCell(table, "So luong");
            addCell(table, "Trang thai");

            for (MedicalSupply supply : medicalSupplyRepository.findAll()) {
                addCell(table, supply.getCode());
                addCell(table, supply.getName());
                addCell(table, String.valueOf(supply.getQuantity()));
                addCell(table, supply.getStatus().name());
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Khong the xuat file PDF", ex);
        }
    }

    @Override
    public byte[] exportStockMovementsToExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("BienDongKho");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Ngay");
            header.createCell(1).setCellValue("Ma vat tu");
            header.createCell(2).setCellValue("Ten vat tu");
            header.createCell(3).setCellValue("Lo");
            header.createCell(4).setCellValue("Loai");
            header.createCell(5).setCellValue("Bien dong");
            header.createCell(6).setCellValue("Ton truoc");
            header.createCell(7).setCellValue("Ton sau");
            header.createCell(8).setCellValue("Chung tu");
            header.createCell(9).setCellValue("Nguoi thuc hien");
            header.createCell(10).setCellValue("Ghi chu");

            int rowIdx = 1;
            for (StockMovement movement : stockMovementRepository.findAllByOrderByCreatedAtDesc()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(movement.getMovementDate()));
                row.createCell(1).setCellValue(movement.getSupplyCode());
                row.createCell(2).setCellValue(movement.getSupplyName());
                row.createCell(3).setCellValue(movement.getBatchNumber() == null ? "" : movement.getBatchNumber());
                row.createCell(4).setCellValue(movement.getMovementType().name());
                row.createCell(5).setCellValue(movement.getQuantityChange());
                row.createCell(6).setCellValue(movement.getQuantityBeforeChange());
                row.createCell(7).setCellValue(movement.getQuantityAfterChange());
                row.createCell(8).setCellValue(movement.getReferenceCode());
                row.createCell(9).setCellValue(movement.getActor());
                row.createCell(10).setCellValue(movement.getNote() == null ? "" : movement.getNote());
            }

            for (int i = 0; i <= 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the xuat file Excel bien dong kho", ex);
        }
    }

    @Override
    public byte[] exportStockMovementsToPdf() {
        Document document = new Document();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("BAO CAO BIEN DONG KHO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Ngay lap: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            addCell(table, "Ngay");
            addCell(table, "Ma");
            addCell(table, "Ten");
            addCell(table, "Loai");
            addCell(table, "SL");
            addCell(table, "Chung tu");

            for (StockMovement movement : stockMovementRepository.findAllByOrderByCreatedAtDesc()) {
                addCell(table, String.valueOf(movement.getMovementDate()));
                addCell(table, movement.getSupplyCode());
                addCell(table, movement.getSupplyName());
                addCell(table, movement.getMovementType().name());
                addCell(table, String.valueOf(movement.getQuantityChange()));
                addCell(table, movement.getReferenceCode());
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Khong the xuat file PDF bien dong kho", ex);
        }
    }

    private void addCell(PdfPTable table, String value) {
        table.addCell(new PdfPCell(new Phrase(value)));
    }

    private Map<Integer, Double> convertToMonthMap(List<Object[]> rows) {
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        return result;
    }
}
