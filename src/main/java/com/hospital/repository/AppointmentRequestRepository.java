package com.hospital.repository;

import com.hospital.entity.AppointmentRequest;
import com.hospital.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {

    Optional<AppointmentRequest> findByQrCode(String qrCode);

    long countByAppointmentDate(LocalDate appointmentDate);

    List<AppointmentRequest> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(AppointmentStatus status);

    List<AppointmentRequest> findTop10ByAppointmentDateAndStatusOrderByCreatedAtAsc(LocalDate appointmentDate, AppointmentStatus status);

    List<AppointmentRequest> findByPhoneOrderByCreatedAtDesc(String phone);

    List<AppointmentRequest> findByAppointmentDateOrderByAppointmentTimeAscCreatedAtAsc(LocalDate appointmentDate);

    @Query("""
            select a.department, count(a)
            from AppointmentRequest a
            where a.createdAt >= :fromDate
            group by a.department
            order by count(a) desc
            """)
    List<Object[]> summarizeDepartmentDemand(@Param("fromDate") java.time.LocalDateTime fromDate);
}
