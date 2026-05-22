package com.hospital.repository;

import com.hospital.entity.InboundReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundReceiptDetailRepository extends JpaRepository<InboundReceiptDetail, Long> {

    boolean existsByMedicalSupply_Id(Long medicalSupplyId);
}
