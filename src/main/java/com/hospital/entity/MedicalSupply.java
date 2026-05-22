package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Thông tin chính của vật tư y tế trong kho.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vat_tu")
public class MedicalSupply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã vật tư không được để trống")
    @Column(name = "ma_vat_tu", nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Tên vật tư không được để trống")
    @Column(name = "ten_vat_tu", nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_vat_tu_id", nullable = false)
    @NotNull(message = "Vui lòng chọn loại vật tư")
    private SupplyCategory category;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    @Column(name = "so_luong", nullable = false)
    private Integer quantity;

    @NotBlank(message = "Đơn vị không được để trống")
    @Column(name = "don_vi", nullable = false, length = 50)
    private String unit;

    @Column(name = "muc_ton_toi_thieu", nullable = false)
    private Integer minimumStock = 10;

    @Column(name = "don_gia_uoc_tinh", precision = 18, scale = 2)
    private java.math.BigDecimal estimatedUnitPrice = java.math.BigDecimal.ZERO;

    @NotNull(message = "Ngày nhập không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_nhap", nullable = false)
    private LocalDate importDate;

    @NotNull(message = "Ngày hết hạn không được để trống")
    @Future(message = "Ngày hết hạn phải lớn hơn ngày hiện tại")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_het_han", nullable = false)
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nha_cung_cap_id", nullable = false)
    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Supplier supplier;

    @Column(name = "ma_qr", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 30)
    private SupplyStatus status = SupplyStatus.AVAILABLE;

    /**
     * Tự cập nhật trạng thái trước khi lưu/sửa dữ liệu.
     */
    @PrePersist
    @PreUpdate
    public void updateStatus() {
        if (quantity == null || quantity <= 0) {
            status = SupplyStatus.OUT_OF_STOCK;
            return;
        }
        if (quantity <= minimumStock) {
            status = SupplyStatus.LOW_STOCK;
            return;
        }
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(30))) {
            status = SupplyStatus.EXPIRING_SOON;
            return;
        }
        status = SupplyStatus.AVAILABLE;
    }
}
