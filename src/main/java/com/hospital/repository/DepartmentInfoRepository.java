package com.hospital.repository;

import com.hospital.entity.DepartmentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentInfoRepository extends JpaRepository<DepartmentInfo, Long> {

    Optional<DepartmentInfo> findByNameIgnoreCase(String name);

    Optional<DepartmentInfo> findByCode(String code);
}
