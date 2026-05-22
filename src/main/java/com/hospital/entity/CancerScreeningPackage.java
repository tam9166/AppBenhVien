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
@Table(name = "cancer_screening_packages")
public class CancerScreeningPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(name = "target_group", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String targetGroup;

    @Column(name = "included_services", nullable = false, length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String includedServices;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;
}
