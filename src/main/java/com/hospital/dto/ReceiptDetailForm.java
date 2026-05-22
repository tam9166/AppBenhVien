package com.hospital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents one supply line inside an inbound or outbound receipt form.
 */
@Getter
@Setter
public class ReceiptDetailForm {

    private Long medicalSupplyId;

    @Min(value = 1, message = "So luong phai lon hon 0")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Don gia phai lon hon 0")
    private BigDecimal unitPrice;

    private String batchNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufactureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    public boolean isEmpty() {
        return medicalSupplyId == null
                && quantity == null
                && unitPrice == null
                && !StringUtils.hasText(batchNumber)
                && manufactureDate == null
                && expiryDate == null;
    }

    public boolean hasCoreValues() {
        return medicalSupplyId != null && quantity != null && unitPrice != null;
    }
}
