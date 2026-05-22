package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nhà cung cấp vật tư.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nha_cung_cap")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_nha_cung_cap", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "ten_nha_cung_cap", nullable = false, length = 150)
    private String name;

    @Column(name = "so_dien_thoai", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dia_chi", length = 255)
    private String address;
}
