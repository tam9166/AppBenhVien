package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "medical_service_prices")
public class MedicalServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "service_name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String serviceName;

    @Column(name = "min_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 18, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;
}
