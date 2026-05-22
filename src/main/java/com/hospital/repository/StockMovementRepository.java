package com.hospital.repository;

import com.hospital.entity.StockMovement;
import com.hospital.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findTop20ByOrderByCreatedAtDesc();

    List<StockMovement> findAllByOrderByCreatedAtDesc();

    @Query("""
            select m.supplyCode, m.supplyName, coalesce(sum(abs(m.quantityChange)), 0)
            from StockMovement m
            where m.movementType = :movementType
              and m.movementDate >= :fromDate
            group by m.supplyCode, m.supplyName
            order by coalesce(sum(abs(m.quantityChange)), 0) desc
            """)
    List<Object[]> summarizeUsageFromDate(@Param("movementType") StockMovementType movementType,
                                          @Param("fromDate") LocalDate fromDate);

    @Query("""
            select m.supplyCode, m.supplyName, coalesce(sum(abs(m.quantityChange)), 0)
            from StockMovement m
            where m.movementType = com.hospital.entity.StockMovementType.OUTBOUND
              and m.movementDate = :movementDate
            group by m.supplyCode, m.supplyName
            order by coalesce(sum(abs(m.quantityChange)), 0) desc
            """)
    List<Object[]> summarizeOutboundByDate(@Param("movementDate") LocalDate movementDate);

    @Query("""
            select m
            from StockMovement m
            where m.supplyCode = :supplyCode
            order by m.createdAt desc
            """)
    List<StockMovement> findRecentBySupplyCode(@Param("supplyCode") String supplyCode);
}
