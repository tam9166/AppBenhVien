package com.hospital.repository;

import com.hospital.entity.MedicalSupply;
import com.hospital.entity.SupplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MedicalSupplyRepository extends JpaRepository<MedicalSupply, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "supplier"})
    Page<MedicalSupply> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"category", "supplier"})
    Optional<MedicalSupply> findById(Long id);

    Optional<MedicalSupply> findByCode(String code);

    Optional<MedicalSupply> findByQrCode(String qrCode);

    @EntityGraph(attributePaths = {"category", "supplier"})
    @Query("""
            select m from MedicalSupply m
            join m.category c
            join m.supplier s
            where lower(m.code) like lower(concat('%', :keyword, '%'))
               or lower(m.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%'))
               or lower(s.name) like lower(concat('%', :keyword, '%'))
            """)
    Page<MedicalSupply> search(@Param("keyword") String keyword, Pageable pageable);

    long countByCategory_NameContainingIgnoreCase(String keyword);

    long countByExpiryDateBefore(LocalDate date);

    long countByQuantityLessThanEqual(Integer minimumStock);

    long countByStatus(SupplyStatus status);

    List<MedicalSupply> findTop10ByOrderByQuantityAsc();

    @Query("select m from MedicalSupply m where m.expiryDate <= :date order by m.expiryDate asc")
    List<MedicalSupply> findExpiringSupplies(@Param("date") LocalDate date);

    @Query("select m from MedicalSupply m where m.quantity <= m.minimumStock order by m.quantity asc")
    List<MedicalSupply> findLowStockSupplies();

    @Query("""
            select c.name, count(m)
            from MedicalSupply m
            join m.category c
            group by c.name
            order by c.name
            """)
    List<Object[]> getCategoryDistribution();
}
