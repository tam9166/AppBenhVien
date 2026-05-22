package com.hospital.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiếu xuất kho.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phieu_xuat")
public class OutboundReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu_xuat", nullable = false, unique = true, length = 30)
    private String receiptCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_xuat", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "nguoi_xuat", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "khoa_nhan", length = 100)
    private String departmentName;

    @Column(name = "ghi_chu", length = 255)
    private String note;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OutboundReceiptDetail> details = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (receiptDate == null) {
            receiptDate = LocalDate.now();
        }
    }
}
