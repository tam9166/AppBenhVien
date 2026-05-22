package com.hospital.repository;

import com.hospital.entity.SupplyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyCategoryRepository extends JpaRepository<SupplyCategory, Long> {
    List<SupplyCategory> findAllByOrderByNameAsc();
}
