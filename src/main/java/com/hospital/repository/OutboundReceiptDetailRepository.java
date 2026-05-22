package com.hospital.repository;

import com.hospital.entity.OutboundReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboundReceiptDetailRepository extends JpaRepository<OutboundReceiptDetail, Long> {

    boolean existsByMedicalSupply_Id(Long medicalSupplyId);

    @Query("""
            select d.medicalSupply.name, coalesce(sum(d.quantity), 0)
            from OutboundReceiptDetail d
            group by d.medicalSupply.name
            order by coalesce(sum(d.quantity), 0) desc
            """)
    List<Object[]> findTopUsedSupplies();
}
