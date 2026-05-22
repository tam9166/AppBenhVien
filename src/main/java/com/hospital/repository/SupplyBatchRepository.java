package com.hospital.repository;

import com.hospital.entity.MedicalSupply;
import com.hospital.entity.SupplyBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SupplyBatchRepository extends JpaRepository<SupplyBatch, Long> {

    List<SupplyBatch> findByMedicalSupplyOrderByExpiryDateAscBatchNumberAsc(MedicalSupply medicalSupply);

    boolean existsByMedicalSupply_Id(Long medicalSupplyId);

    @Query("""
            select b from SupplyBatch b
            join fetch b.medicalSupply
            where b.quantity > 0 and b.expiryDate <= :threshold
            order by b.expiryDate asc
            """)
    List<SupplyBatch> findExpiringBatches(@Param("threshold") LocalDate threshold);

    @Query("""
            select coalesce(sum(b.quantity), 0)
            from SupplyBatch b
            where b.medicalSupply.id = :supplyId
            """)
    Integer sumQuantityBySupplyId(@Param("supplyId") Long supplyId);
}
