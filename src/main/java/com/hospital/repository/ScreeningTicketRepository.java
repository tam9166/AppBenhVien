package com.hospital.repository;

import com.hospital.entity.ScreeningTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningTicketRepository extends JpaRepository<ScreeningTicket, Long> {

    long countByEmergencyTrue();

    List<ScreeningTicket> findTop10ByOrderByCreatedAtDesc();
}
