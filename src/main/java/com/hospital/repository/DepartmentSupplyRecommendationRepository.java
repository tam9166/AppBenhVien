package com.hospital.repository;

import com.hospital.entity.DepartmentSupplyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentSupplyRecommendationRepository extends JpaRepository<DepartmentSupplyRecommendation, Long> {

    List<DepartmentSupplyRecommendation> findByDepartmentContainingIgnoreCaseOrderByPriorityAsc(String department);
}
