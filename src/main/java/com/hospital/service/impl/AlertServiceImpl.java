package com.hospital.service.impl;

import com.hospital.dto.DashboardAlertDto;
import com.hospital.entity.AlertNotification;
import com.hospital.entity.AlertType;
import com.hospital.entity.AppointmentRequest;
import com.hospital.entity.MedicalSupply;
import com.hospital.entity.StockMovementType;
import com.hospital.entity.SupplyBatch;
import com.hospital.repository.AlertNotificationRepository;
import com.hospital.repository.AppointmentRequestRepository;
import com.hospital.service.AlertService;
import com.hospital.service.MedicalSupplyService;
import com.hospital.service.StockMovementService;
import com.hospital.service.SupplyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private static final String LOW_STOCK_TITLE = "Vật tư dưới mức tồn kho";
    private static final String EXPIRING_BATCH_TITLE = "Lô vật tư sắp hết hạn";
    private static final String CONSUMPTION_RISK_TITLE = "Vật tư có nguy cơ hết hàng theo tốc độ dùng";
    private static final String UNUSUAL_OUTBOUND_TITLE = "Xuất kho bất thường";
    private static final String APPOINTMENT_REQUEST_TITLE = "Lịch khám mới từ cổng bệnh nhân";
    private static final String APPOINTMENT_REMINDER_TITLE = "Lịch khám cần nhắc trong ngày mai";

    private final AlertNotificationRepository alertNotificationRepository;
    private final AppointmentRequestRepository appointmentRequestRepository;
    private final MedicalSupplyService medicalSupplyService;
    private final SupplyBatchService supplyBatchService;
    private final StockMovementService stockMovementService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void warmupAlerts() {
        refreshAlerts();
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 6 * * *")
    public void refreshAlerts() {
        Set<String> activeKeys = new HashSet<>();

        for (MedicalSupply supply : medicalSupplyService.getLowStockSupplies()) {
            String referenceCode = supply.getCode();
            activeKeys.add(AlertType.LOW_STOCK + ":" + referenceCode);
            AlertNotification alert = alertNotificationRepository
                    .findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType.LOW_STOCK, referenceCode)
                    .orElseGet(AlertNotification::new);
            alert.setAlertType(AlertType.LOW_STOCK);
            alert.setReferenceCode(referenceCode);
            alert.setTitle(LOW_STOCK_TITLE);
            alert.setMessage("Vật tư " + supply.getName() + " chỉ còn " + supply.getQuantity() + " " + supply.getUnit()
                    + ", thấp hơn mức tối thiểu " + supply.getMinimumStock() + ".");
            alert.setResolved(false);
            alertNotificationRepository.save(alert);
        }

        for (SupplyBatch batch : supplyBatchService.getExpiringBatches(LocalDate.now().plusDays(30))) {
            String referenceCode = batch.getMedicalSupply().getCode() + "-" + batch.getBatchNumber();
            activeKeys.add(AlertType.EXPIRING_BATCH + ":" + referenceCode);
            AlertNotification alert = alertNotificationRepository
                    .findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType.EXPIRING_BATCH, referenceCode)
                    .orElseGet(AlertNotification::new);
            alert.setAlertType(AlertType.EXPIRING_BATCH);
            alert.setReferenceCode(referenceCode);
            alert.setTitle(EXPIRING_BATCH_TITLE);
            alert.setMessage("Lô " + batch.getBatchNumber() + " của " + batch.getMedicalSupply().getName()
                    + " sẽ hết hạn vào " + batch.getExpiryDate() + ".");
            alert.setResolved(false);
            alertNotificationRepository.save(alert);
        }

        for (MedicalSupply supply : medicalSupplyService.getLowStockSupplies()) {
            long averageDailyUsage = resolveAverageDailyOutbound(supply.getCode(), 30);
            if (averageDailyUsage <= 0) {
                continue;
            }
            long remainingDays = supply.getQuantity() / Math.max(averageDailyUsage, 1L);
            if (remainingDays > 7) {
                continue;
            }
            String referenceCode = supply.getCode();
            activeKeys.add(AlertType.CONSUMPTION_RISK + ":" + referenceCode);
            AlertNotification alert = alertNotificationRepository
                    .findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType.CONSUMPTION_RISK, referenceCode)
                    .orElseGet(AlertNotification::new);
            alert.setAlertType(AlertType.CONSUMPTION_RISK);
            alert.setReferenceCode(referenceCode);
            alert.setTitle(CONSUMPTION_RISK_TITLE);
            alert.setMessage("Vật tư " + supply.getName() + " có tốc độ sử dụng trung bình khoảng " + averageDailyUsage
                    + " đơn vị/ngày và chỉ còn đủ khoảng " + remainingDays + " ngày.");
            alert.setResolved(false);
            alertNotificationRepository.save(alert);
        }

        for (Object[] row : stockMovementService.summarizeOutboundByDate(LocalDate.now())) {
            String supplyCode = String.valueOf(row[0]);
            String supplyName = String.valueOf(row[1]);
            long todayQuantity = ((Number) row[2]).longValue();
            long averageDailyUsage = resolveAverageDailyOutbound(supplyCode, 7);
            if (averageDailyUsage <= 0 || todayQuantity < averageDailyUsage * 2) {
                continue;
            }
            activeKeys.add(AlertType.UNUSUAL_OUTBOUND + ":" + supplyCode);
            AlertNotification alert = alertNotificationRepository
                    .findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType.UNUSUAL_OUTBOUND, supplyCode)
                    .orElseGet(AlertNotification::new);
            alert.setAlertType(AlertType.UNUSUAL_OUTBOUND);
            alert.setReferenceCode(supplyCode);
            alert.setTitle(UNUSUAL_OUTBOUND_TITLE);
            alert.setMessage("Vật tư " + supplyName + " có lượng xuất hôm nay " + todayQuantity
                    + ", cao bất thường so với mức trung bình gần đây khoảng " + averageDailyUsage + ".");
            alert.setResolved(false);
            alertNotificationRepository.save(alert);
        }

        for (AppointmentRequest appointment : appointmentRequestRepository.findByAppointmentDateOrderByAppointmentTimeAscCreatedAtAsc(LocalDate.now().plusDays(1))) {
            String referenceCode = appointment.getQrCode();
            activeKeys.add(AlertType.APPOINTMENT_REMINDER + ":" + referenceCode);
            AlertNotification alert = alertNotificationRepository
                    .findByAlertTypeAndReferenceCodeAndResolvedFalse(AlertType.APPOINTMENT_REMINDER, referenceCode)
                    .orElseGet(AlertNotification::new);
            alert.setAlertType(AlertType.APPOINTMENT_REMINDER);
            alert.setReferenceCode(referenceCode);
            alert.setTitle(APPOINTMENT_REMINDER_TITLE);
            alert.setMessage("Bệnh nhân " + appointment.getPatientName()
                    + " có lịch khám khoa " + appointment.getDepartment()
                    + " vào ngày mai lúc " + appointment.getAppointmentTime() + ".");
            alert.setResolved(false);
            alertNotificationRepository.save(alert);
        }

        for (AlertNotification existing : alertNotificationRepository.findByResolvedFalse()) {
            if (existing.getAlertType() == AlertType.APPOINTMENT_REQUEST) {
                continue;
            }
            String key = existing.getAlertType() + ":" + existing.getReferenceCode();
            if (!activeKeys.contains(key)) {
                existing.setResolved(true);
                alertNotificationRepository.save(existing);
            }
        }
    }

    @Override
    public List<DashboardAlertDto> getRecentActiveAlerts() {
        return alertNotificationRepository.findTop10ByResolvedFalseOrderByLastDetectedAtDesc().stream()
                .map(alert -> new DashboardAlertDto(
                        alert.getAlertType(),
                        alert.getTitle(),
                        alert.getMessage(),
                        alert.getReferenceCode(),
                        alert.getLastDetectedAt()))
                .toList();
    }

    @Override
    public long countActiveAlerts() {
        return alertNotificationRepository.countByResolvedFalse();
    }

    @Override
    @Transactional
    public void createAppointmentRequestAlert(AppointmentRequest appointmentRequest) {
        AlertNotification alert = new AlertNotification();
        alert.setAlertType(AlertType.APPOINTMENT_REQUEST);
        alert.setReferenceCode(appointmentRequest.getQrCode());
        alert.setTitle(APPOINTMENT_REQUEST_TITLE);
        alert.setMessage("Bệnh nhân " + appointmentRequest.getPatientName()
                + " vừa đặt lịch khám khoa " + appointmentRequest.getDepartment()
                + " vào " + appointmentRequest.getAppointmentDate()
                + " " + appointmentRequest.getAppointmentTime() + ".");
        alert.setResolved(false);
        alertNotificationRepository.save(alert);
    }

    private long resolveAverageDailyOutbound(String supplyCode, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        return stockMovementService.summarizeUsageFromDate(StockMovementType.OUTBOUND, fromDate).stream()
                .filter(row -> supplyCode.equals(String.valueOf(row[0])))
                .mapToLong(row -> ((Number) row[2]).longValue() / Math.max(days, 1))
                .findFirst()
                .orElse(0L);
    }
}
