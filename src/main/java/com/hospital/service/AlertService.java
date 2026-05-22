package com.hospital.service;

import com.hospital.dto.DashboardAlertDto;
import com.hospital.entity.AppointmentRequest;

import java.util.List;

public interface AlertService {
    void refreshAlerts();

    List<DashboardAlertDto> getRecentActiveAlerts();

    long countActiveAlerts();

    void createAppointmentRequestAlert(AppointmentRequest appointmentRequest);
}
