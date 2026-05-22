package com.hospital.service;

import com.hospital.dto.DashboardStats;

public interface DashboardService {
    DashboardStats buildDashboardStats(int year, int month);
}
