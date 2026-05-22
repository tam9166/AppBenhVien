package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dòng chi tiết của phiếu nhập.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chi_tiet_phieu_nhap")
public class InboundReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_nhap_id", nullable = false)
    private InboundReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vat_tu_id", nullable = false)
    private MedicalSupply medicalSupply;

    @Column(name = "so_luong", nullable = false)
    private Integer quantity;

    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "so_lo", nullable = true, length = 50)
    private String batchNumber;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_san_xuat")
    private LocalDate manufactureDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "han_su_dung_lo", nullable = true)
    private LocalDate batchExpiryDate;
}
