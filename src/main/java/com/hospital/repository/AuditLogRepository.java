package com.hospital.repository;

import com.hospital.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByCreatedAtDesc();

    @Query("""
            select a from AuditLog a
            where a.createdAt >= :fromDate
            order by a.createdAt desc
            """)
    List<AuditLog> findRecentLogs(@Param("fromDate") LocalDateTime fromDate);
}
