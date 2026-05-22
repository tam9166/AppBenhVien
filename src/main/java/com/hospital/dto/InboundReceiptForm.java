package com.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO tạo phiếu nhập từ giao diện.
 */
@Getter
@Setter
public class InboundReceiptForm {

    @NotBlank(message = "Mã phiếu nhập không được để trống")
    private String receiptCode;

    @NotNull(message = "Ngày nhập không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate receiptDate;

    @NotBlank(message = "Người nhập không được để trống")
    private String createdBy;

    private String note;

    @Valid
    private List<ReceiptDetailForm> details = new ArrayList<>();
}
