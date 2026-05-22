package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "department_supply_recommendations")
public class DepartmentSupplyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "supply_code", nullable = false, length = 30)
    private String supplyCode;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1;
}
