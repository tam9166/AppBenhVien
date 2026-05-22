package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supply_code", nullable = false, length = 50)
    private String supplyCode;

    @Column(name = "supply_name", nullable = false, length = 150)
    private String supplyName;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_before_change", nullable = false)
    private Integer quantityBeforeChange;

    @Column(name = "quantity_after_change", nullable = false)
    private Integer quantityAfterChange;

    @Column(name = "reference_code", nullable = false, length = 50)
    private String referenceCode;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (movementDate == null) {
            movementDate = now.toLocalDate();
        }
    }
}
