package com.hospital.repository;

import com.hospital.entity.AlertNotification;
import com.hospital.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertNotificationRepository extends JpaRepository<AlertNotification, Long> {
    Optional<AlertNotification> findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType alertType, String referenceCode);

    List<AlertNotification> findTop10ByResolvedFalseOrderByLastDetectedAtDesc();

    List<AlertNotification> findByResolvedFalse();

    long countByResolvedFalse();

    List<AlertNotification> findTop10ByAlertTypeAndResolvedFalseOrderByLastDetectedAtDesc(AlertType alertType);

    long countByAlertTypeAndResolvedFalse(AlertType alertType);
}
