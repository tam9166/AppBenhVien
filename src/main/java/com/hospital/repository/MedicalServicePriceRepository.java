package com.hospital.repository;

import com.hospital.entity.MedicalServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalServicePriceRepository extends JpaRepository<MedicalServicePrice, Long> {

    Optional<MedicalServicePrice> findByCode(String code);
}
