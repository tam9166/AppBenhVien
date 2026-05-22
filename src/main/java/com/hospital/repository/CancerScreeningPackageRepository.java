package com.hospital.repository;

import com.hospital.entity.CancerScreeningPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancerScreeningPackageRepository extends JpaRepository<CancerScreeningPackage, Long> {

    Optional<CancerScreeningPackage> findByCode(String code);
}
