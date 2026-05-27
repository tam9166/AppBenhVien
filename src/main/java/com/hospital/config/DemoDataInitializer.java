package com.hospital.config;

import com.hospital.entity.AlertNotification;
import com.hospital.entity.AlertType;
import com.hospital.entity.AuditLog;
import com.hospital.entity.DepartmentInfo;
import com.hospital.entity.DepartmentSupplyRecommendation;
import com.hospital.entity.InboundReceipt;
import com.hospital.entity.InboundReceiptDetail;
import com.hospital.entity.CancerScreeningPackage;
import com.hospital.entity.MedicalServicePrice;
import com.hospital.entity.MedicalSupply;
import com.hospital.entity.OutboundReceipt;
import com.hospital.entity.OutboundReceiptDetail;
import com.hospital.entity.Supplier;
import com.hospital.entity.SupplyBatch;
import com.hospital.entity.SupplyCategory;
import com.hospital.repository.AlertNotificationRepository;
import com.hospital.repository.AuditLogRepository;
import com.hospital.repository.DepartmentInfoRepository;
import com.hospital.repository.DepartmentSupplyRecommendationRepository;
import com.hospital.repository.InboundReceiptRepository;
import com.hospital.repository.CancerScreeningPackageRepository;
import com.hospital.repository.MedicalServicePriceRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.repository.OutboundReceiptRepository;
import com.hospital.repository.SupplierRepository;
import com.hospital.repository.SupplyBatchRepository;
import com.hospital.repository.SupplyCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private static final int TARGET_SUPPLIES = 18;
    private static final int TARGET_INBOUND_RECEIPTS = 16;
    private static final int TARGET_OUTBOUND_RECEIPTS = 16;
    private static final int TARGET_ALERTS = 16;
    private static final int TARGET_AUDIT_LOGS = 18;

    private final SupplyCategoryRepository supplyCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final SupplyBatchRepository supplyBatchRepository;
    private final InboundReceiptRepository inboundReceiptRepository;
    private final OutboundReceiptRepository outboundReceiptRepository;
    private final AlertNotificationRepository alertNotificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final DepartmentInfoRepository departmentInfoRepository;
    private final MedicalServicePriceRepository medicalServicePriceRepository;
    private final CancerScreeningPackageRepository cancerScreeningPackageRepository;
    private final DepartmentSupplyRecommendationRepository departmentSupplyRecommendationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureAlertTypeConstraint();
        seedCategories();
        seedSuppliers();
        seedDepartments();
        seedServicePrices();
        seedScreeningPackages();
        seedDepartmentSupplyRecommendations();
        repairPatientPortalVietnameseText();
        seedMedicalSupplies();
        seedBatches();
        seedInboundReceipts();
        seedOutboundReceipts();
        seedAlerts();
        seedAuditLogs();
    }

    private void seedDepartments() {
        if (departmentInfoRepository.count() > 0) {
            return;
        }

        List<DepartmentSeed> seeds = List.of(
                new DepartmentSeed("CAPCUU", "Cấp cứu", "Tầng trệt - Khu A", "24/7", "1900 1234", "Tiếp nhận các tình huống nguy hiểm cần xử trí ngay."),
                new DepartmentSeed("TIMMACH", "Tim mạch", "Tầng 2 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám đau ngực, hồi hộp, tăng huyết áp và bệnh lý tim mạch."),
                new DepartmentSeed("THANKINH", "Thần kinh", "Tầng 3 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám đau đầu, chóng mặt, tê yếu tay chân, dấu hiệu đột quỵ."),
                new DepartmentSeed("HOHAP", "Hô hấp", "Tầng 2 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám ho kéo dài, khó thở, khò khè và bệnh đường hô hấp."),
                new DepartmentSeed("TAIMUIHONG", "Tai mũi họng", "Tầng 1 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám đau họng, nghẹt mũi, đau tai và viêm xoang."),
                new DepartmentSeed("COXUONGKHOP", "Cơ xương khớp", "Tầng 4 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám đau khớp, đau tay chân, chấn thương nhẹ."),
                new DepartmentSeed("TIEUHOA", "Tiêu hóa", "Tầng 3 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám đau bụng, nôn, tiêu chảy và bệnh dạ dày."),
                new DepartmentSeed("DALIEU", "Da liễu", "Tầng 1 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám nổi mẩn, ngứa, phát ban và bệnh lý da."),
                new DepartmentSeed("NHIKHOA", "Nhi khoa", "Tầng 2 - Khu D", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám bệnh cho trẻ em."),
                new DepartmentSeed("SANPHU", "Sản phụ khoa", "Tầng 3 - Khu D", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Khám thai kỳ, phụ khoa và sức khỏe sinh sản."),
                new DepartmentSeed("NOITONGQUAT", "Nội tổng quát", "Tầng 1 - Khu A", "7h00-17h00 từ thứ 2 đến thứ 7", "1900 1234", "Tiếp nhận triệu chứng chưa rõ chuyên khoa.")
        );

        for (DepartmentSeed seed : seeds) {
            DepartmentInfo info = new DepartmentInfo();
            info.setCode(seed.code());
            info.setName(seed.name());
            info.setLocation(seed.location());
            info.setWorkingHours(seed.workingHours());
            info.setHotline(seed.hotline());
            info.setDescription(seed.description());
            departmentInfoRepository.save(info);
        }
    }

    private void seedServicePrices() {
        if (medicalServicePriceRepository.count() > 0) {
            return;
        }

        List<ServicePriceSeed> seeds = List.of(
                new ServicePriceSeed("KHAM_THUONG", "Khám thường", 100000, 150000, "Áp dụng trong giờ hành chính, chưa bao gồm xét nghiệm/cận lâm sàng."),
                new ServicePriceSeed("KHAM_CHUYEN_KHOA", "Khám chuyên khoa", 150000, 250000, "Chi phí có thể thay đổi theo chuyên khoa và bác sĩ phụ trách."),
                new ServicePriceSeed("KHAM_NGOAI_GIO", "Khám ngoài giờ", 200000, 350000, "Áp dụng ngoài khung giờ hành chính hoặc theo lịch trực."),
                new ServicePriceSeed("CAP_CUU", "Tiếp nhận cấp cứu", 300000, 500000, "Chưa bao gồm thuốc, vật tư, xét nghiệm và thủ thuật phát sinh."),
                new ServicePriceSeed("TAI_KHAM", "Tái khám", 80000, 120000, "Tùy chương trình điều trị và thời gian tái khám.")
        );

        for (ServicePriceSeed seed : seeds) {
            MedicalServicePrice price = new MedicalServicePrice();
            price.setCode(seed.code());
            price.setServiceName(seed.serviceName());
            price.setMinPrice(BigDecimal.valueOf(seed.minPrice()));
            price.setMaxPrice(BigDecimal.valueOf(seed.maxPrice()));
            price.setNote(seed.note());
            medicalServicePriceRepository.save(price);
        }
    }

    private void seedScreeningPackages() {
        if (cancerScreeningPackageRepository.count() > 0) {
            return;
        }

        List<ScreeningPackageSeed> seeds = List.of(
                new ScreeningPackageSeed("TS-VU", "Tầm soát ung thư vú", "Phụ nữ từ 40 tuổi hoặc có yếu tố nguy cơ", "Khám chuyên khoa, siêu âm tuyến vú, nhũ ảnh theo chỉ định", 1200000, "Nên đặt lịch trước để được hướng dẫn chuẩn bị."),
                new ScreeningPackageSeed("TS-PHOI", "Tầm soát ung thư phổi", "Người hút thuốc lâu năm hoặc có triệu chứng hô hấp kéo dài", "Khám hô hấp, X-quang/CT theo chỉ định, tư vấn nguy cơ", 1800000, "Chi phí có thể thay đổi theo cận lâm sàng."),
                new ScreeningPackageSeed("TS-TIEUHOA", "Tầm soát ung thư tiêu hóa", "Người đau bụng kéo dài, rối loạn tiêu hóa hoặc tiền sử gia đình", "Khám tiêu hóa, xét nghiệm máu, nội soi theo chỉ định", 2200000, "Cần tư vấn trước nếu nội soi gây mê."),
                new ScreeningPackageSeed("TS-TUYENGIAP", "Tầm soát ung thư tuyến giáp", "Người có nhân giáp, bướu cổ hoặc tiền sử gia đình", "Khám chuyên khoa, siêu âm tuyến giáp, xét nghiệm theo chỉ định", 900000, "Phù hợp cho khám định kỳ.")
        );

        for (ScreeningPackageSeed seed : seeds) {
            CancerScreeningPackage item = new CancerScreeningPackage();
            item.setCode(seed.code());
            item.setName(seed.name());
            item.setTargetGroup(seed.targetGroup());
            item.setIncludedServices(seed.includedServices());
            item.setPrice(BigDecimal.valueOf(seed.price()));
            item.setNote(seed.note());
            cancerScreeningPackageRepository.save(item);
        }
    }

    private void seedDepartmentSupplyRecommendations() {
        if (departmentSupplyRecommendationRepository.count() > 0) {
            return;
        }

        List<DepartmentSupplySeed> seeds = List.of(
                new DepartmentSupplySeed("Cấp cứu", "VT002", "Găng tay cần chuẩn bị nhiều cho tiếp nhận cấp cứu.", 1),
                new DepartmentSupplySeed("Cấp cứu", "VT010", "Kim luồn tĩnh mạch thường dùng trong xử trí ban đầu.", 2),
                new DepartmentSupplySeed("Hô hấp", "VT001", "Khẩu trang dùng cho khu vực tiếp nhận triệu chứng hô hấp.", 1),
                new DepartmentSupplySeed("Hô hấp", "VT018", "Bộ test cúm nhanh hỗ trợ sàng lọc triệu chứng hô hấp.", 2),
                new DepartmentSupplySeed("Tiêu hóa", "VT006", "Dung dịch NaCl hỗ trợ xử trí mất nước theo chỉ định.", 1),
                new DepartmentSupplySeed("Nội tổng quát", "VT003", "Thuốc thông dụng cần theo dõi tồn kho an toàn.", 1)
        );

        for (DepartmentSupplySeed seed : seeds) {
            DepartmentSupplyRecommendation item = new DepartmentSupplyRecommendation();
            item.setDepartment(seed.department());
            item.setSupplyCode(seed.supplyCode());
            item.setReason(seed.reason());
            item.setPriority(seed.priority());
            departmentSupplyRecommendationRepository.save(item);
        }
    }

    private void repairPatientPortalVietnameseText() {
        ensurePatientPortalUnicodeColumns();
        updateDepartment("CAPCUU", "Cấp cứu", "Tầng trệt - Khu A", "24/7", "Tiếp nhận các tình huống nguy hiểm cần xử trí ngay.");
        updateDepartment("TIMMACH", "Tim mạch", "Tầng 2 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám đau ngực, hồi hộp, tăng huyết áp và bệnh lý tim mạch.");
        updateDepartment("THANKINH", "Thần kinh", "Tầng 3 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám đau đầu, chóng mặt, tê yếu tay chân, dấu hiệu đột quỵ.");
        updateDepartment("HOHAP", "Hô hấp", "Tầng 2 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám ho kéo dài, khó thở, khò khè và bệnh đường hô hấp.");
        updateDepartment("TAIMUIHONG", "Tai mũi họng", "Tầng 1 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám đau họng, nghẹt mũi, đau tai và viêm xoang.");
        updateDepartment("COXUONGKHOP", "Cơ xương khớp", "Tầng 4 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám đau khớp, đau tay chân, chấn thương nhẹ.");
        updateDepartment("TIEUHOA", "Tiêu hóa", "Tầng 3 - Khu C", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám đau bụng, nôn, tiêu chảy và bệnh dạ dày.");
        updateDepartment("DALIEU", "Da liễu", "Tầng 1 - Khu B", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám nổi mẩn, ngứa, phát ban và bệnh lý da.");
        updateDepartment("NHIKHOA", "Nhi khoa", "Tầng 2 - Khu D", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám bệnh cho trẻ em.");
        updateDepartment("SANPHU", "Sản phụ khoa", "Tầng 3 - Khu D", "7h00-17h00 từ thứ 2 đến thứ 7", "Khám thai kỳ, phụ khoa và sức khỏe sinh sản.");
        updateDepartment("NOITONGQUAT", "Nội tổng quát", "Tầng 1 - Khu A", "7h00-17h00 từ thứ 2 đến thứ 7", "Tiếp nhận triệu chứng chưa rõ chuyên khoa.");

        updatePrice("KHAM_THUONG", "Khám thường", "Áp dụng trong giờ hành chính, chưa bao gồm xét nghiệm/cận lâm sàng.");
        updatePrice("KHAM_CHUYEN_KHOA", "Khám chuyên khoa", "Chi phí có thể thay đổi theo chuyên khoa và bác sĩ phụ trách.");
        updatePrice("KHAM_NGOAI_GIO", "Khám ngoài giờ", "Áp dụng ngoài khung giờ hành chính hoặc theo lịch trực.");
        updatePrice("CAP_CUU", "Tiếp nhận cấp cứu", "Chưa bao gồm thuốc, vật tư, xét nghiệm và thủ thuật phát sinh.");
        updatePrice("TAI_KHAM", "Tái khám", "Tùy chương trình điều trị và thời gian tái khám.");

        updateScreeningPackage("TS-VU", "Tầm soát ung thư vú", "Phụ nữ từ 40 tuổi hoặc có yếu tố nguy cơ", "Khám chuyên khoa, siêu âm tuyến vú, nhũ ảnh theo chỉ định", "Nên đặt lịch trước để được hướng dẫn chuẩn bị.");
        updateScreeningPackage("TS-PHOI", "Tầm soát ung thư phổi", "Người hút thuốc lâu năm hoặc có triệu chứng hô hấp kéo dài", "Khám hô hấp, X-quang/CT theo chỉ định, tư vấn nguy cơ", "Chi phí có thể thay đổi theo cận lâm sàng.");
        updateScreeningPackage("TS-TIEUHOA", "Tầm soát ung thư tiêu hóa", "Người đau bụng kéo dài, rối loạn tiêu hóa hoặc tiền sử gia đình", "Khám tiêu hóa, xét nghiệm máu, nội soi theo chỉ định", "Cần tư vấn trước nếu nội soi gây mê.");
        updateScreeningPackage("TS-TUYENGIAP", "Tầm soát ung thư tuyến giáp", "Người có nhân giáp, bướu cổ hoặc tiền sử gia đình", "Khám chuyên khoa, siêu âm tuyến giáp, xét nghiệm theo chỉ định", "Phù hợp cho khám định kỳ.");
    }

    private void ensurePatientPortalUnicodeColumns() {
        jdbcTemplate.execute("ALTER TABLE department_infos ALTER COLUMN name NVARCHAR(100) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE department_infos ALTER COLUMN location NVARCHAR(150) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE department_infos ALTER COLUMN working_hours NVARCHAR(150) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE department_infos ALTER COLUMN description NVARCHAR(500) NULL");
        jdbcTemplate.execute("ALTER TABLE medical_service_prices ALTER COLUMN service_name NVARCHAR(150) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE medical_service_prices ALTER COLUMN note NVARCHAR(500) NULL");
        jdbcTemplate.execute("ALTER TABLE cancer_screening_packages ALTER COLUMN name NVARCHAR(150) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE cancer_screening_packages ALTER COLUMN target_group NVARCHAR(200) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE cancer_screening_packages ALTER COLUMN included_services NVARCHAR(1000) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE cancer_screening_packages ALTER COLUMN note NVARCHAR(500) NULL");
        jdbcTemplate.execute("ALTER TABLE appointment_requests ALTER COLUMN patient_name NVARCHAR(120) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE appointment_requests ALTER COLUMN department NVARCHAR(100) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE appointment_requests ALTER COLUMN symptoms NVARCHAR(1000) NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'screening_ticket_id') IS NULL ALTER TABLE appointment_requests ADD screening_ticket_id BIGINT NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'triage_summary') IS NULL ALTER TABLE appointment_requests ADD triage_summary NVARCHAR(2000) NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'priority_level') IS NULL ALTER TABLE appointment_requests ADD priority_level NVARCHAR(50) NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'emergency') IS NULL ALTER TABLE appointment_requests ADD emergency BIT NOT NULL CONSTRAINT DF_appointment_requests_emergency DEFAULT 0");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'queue_number') IS NULL ALTER TABLE appointment_requests ADD queue_number INT NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'admin_note') IS NULL ALTER TABLE appointment_requests ADD admin_note NVARCHAR(1000) NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'confirmed_at') IS NULL ALTER TABLE appointment_requests ADD confirmed_at DATETIME2 NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'checked_in_at') IS NULL ALTER TABLE appointment_requests ADD checked_in_at DATETIME2 NULL");
        jdbcTemplate.execute("IF COL_LENGTH('appointment_requests', 'cancelled_at') IS NULL ALTER TABLE appointment_requests ADD cancelled_at DATETIME2 NULL");
        jdbcTemplate.execute("ALTER TABLE alert_notifications ALTER COLUMN title NVARCHAR(150) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE alert_notifications ALTER COLUMN message NVARCHAR(500) NOT NULL");
        repairScreeningTicketColumns();
        repairScreeningTicketRows();
        jdbcTemplate.execute("UPDATE appointment_requests SET priority_level = N'Thông thường' WHERE priority_level IS NULL OR priority_level LIKE '%Ã%' OR priority_level LIKE '%áº%' OR priority_level LIKE '%á»%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Nội tổng quát' WHERE department LIKE 'N%i t%ng qu%t' OR department LIKE '%N?i t?ng quát%' OR department LIKE '%Noi tong quat%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Cấp cứu' WHERE department LIKE 'C%p c%u' OR department LIKE '%Cap cuu%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Tim mạch' WHERE department LIKE 'Tim m%ch' OR department LIKE '%Tim mach%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Thần kinh' WHERE department LIKE 'Th%n kinh' OR department LIKE '%Than kinh%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Hô hấp' WHERE department LIKE 'H% h%p' OR department LIKE '%Ho hap%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Tiêu hóa' WHERE department LIKE 'Ti%u h%a' OR department LIKE '%Tieu hoa%'");
        jdbcTemplate.execute("UPDATE appointment_requests SET department = N'Da liễu' WHERE department LIKE 'Da li%u' OR department LIKE '%Da lieu%'");
        jdbcTemplate.execute("UPDATE alert_notifications SET title = N'Lịch khám mới từ cổng bệnh nhân' WHERE alert_type = 'APPOINTMENT_REQUEST' AND (title LIKE '%Ã%' OR title LIKE '%áº%' OR title LIKE '%á»%')");
        jdbcTemplate.execute("UPDATE alert_notifications SET title = N'Lịch khám cần nhắc trong ngày mai' WHERE alert_type = 'APPOINTMENT_REMINDER' AND (title LIKE '%Ã%' OR title LIKE '%áº%' OR title LIKE '%á»%')");
    }

    private void updateDepartment(String code, String name, String location, String workingHours, String description) {
        jdbcTemplate.update("""
                UPDATE department_infos
                SET name = ?, location = ?, working_hours = ?, hotline = ?, description = ?
                WHERE code = ?
                """, name, location, workingHours, "1900 1234", description, code);
    }

    private void repairScreeningTicketColumns() {
        jdbcTemplate.execute("ALTER TABLE screening_tickets ALTER COLUMN patient_message NVARCHAR(MAX) NULL");
        jdbcTemplate.execute("ALTER TABLE screening_tickets ALTER COLUMN suggested_department NVARCHAR(100) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE screening_tickets ALTER COLUMN risk_level NVARCHAR(50) NOT NULL");
        jdbcTemplate.execute("ALTER TABLE screening_tickets ALTER COLUMN summary NVARCHAR(MAX) NOT NULL");
    }

    private void repairScreeningTicketRows() {
        jdbcTemplate.execute("UPDATE screening_tickets SET suggested_department = N'N\u1ed9i t\u1ed5ng qu\u00e1t', risk_level = N'nh\u1eb9', summary = N'Vui l\u00f2ng nh\u1eadp c\u00e2u h\u1ecfi ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 t\u00f4i h\u1ed7 tr\u1ee3.' + CHAR(10) + N'B\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi v\u1ec1 gi\u1edd kh\u00e1m, \u0111\u1ecba ch\u1ec9, hotline ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 \u0111\u01b0\u1ee3c s\u00e0ng l\u1ecdc s\u01a1 b\u1ed9.' WHERE (patient_message IS NULL OR LTRIM(RTRIM(patient_message)) = '') AND summary LIKE '%?%'");
        jdbcTemplate.execute("UPDATE screening_tickets SET suggested_department = N'N\u1ed9i t\u1ed5ng qu\u00e1t' WHERE suggested_department LIKE '%Ã%' OR suggested_department LIKE '%áº%' OR suggested_department LIKE '%á»%' OR suggested_department LIKE '%N?i%' OR suggested_department LIKE '%Noi tong quat%'");
        jdbcTemplate.execute("UPDATE screening_tickets SET risk_level = N'nh\u1eb9' WHERE risk_level LIKE '%Ã%' OR risk_level LIKE '%áº%' OR risk_level LIKE '%á»%' OR risk_level LIKE '%nh?%'");
    }

    private void updatePrice(String code, String serviceName, String note) {
        jdbcTemplate.update("""
                UPDATE medical_service_prices
                SET service_name = ?, note = ?
                WHERE code = ?
                """, serviceName, note, code);
    }

    private void updateScreeningPackage(String code, String name, String targetGroup, String includedServices, String note) {
        jdbcTemplate.update("""
                UPDATE cancer_screening_packages
                SET name = ?, target_group = ?, included_services = ?, note = ?
                WHERE code = ?
                """, name, targetGroup, includedServices, note, code);
    }

    private void ensureAlertTypeConstraint() {
        List<String> constraintNames = jdbcTemplate.queryForList("""
                SELECT cc.name
                FROM sys.check_constraints cc
                JOIN sys.tables t ON t.object_id = cc.parent_object_id
                WHERE t.name = 'alert_notifications'
                  AND cc.definition LIKE '%alert_type%'
                """, String.class);

        for (String constraintName : constraintNames) {
            jdbcTemplate.execute("ALTER TABLE alert_notifications DROP CONSTRAINT [" + constraintName + "]");
        }

        String allowedValues = Arrays.stream(AlertType.values())
                .map(type -> "'" + type.name() + "'")
                .reduce((left, right) -> left + ", " + right)
                .orElse("'LOW_STOCK'");

        jdbcTemplate.execute(String.format("""
                ALTER TABLE alert_notifications
                ADD CONSTRAINT CK_alert_notifications_alert_type
                CHECK (alert_type IN (%s))
                """, allowedValues));
    }

    private void seedCategories() {
        List<CategorySeed> seeds = List.of(
                new CategorySeed("LOAI001", "Thuốc", "Nhóm thuốc điều trị và thuốc cấp cứu"),
                new CategorySeed("LOAI002", "Vật tư tiêu hao", "Bông, gạc, khẩu trang, găng tay"),
                new CategorySeed("LOAI003", "Thiết bị nhỏ", "Dụng cụ y tế kích thước nhỏ"),
                new CategorySeed("LOAI004", "Dịch truyền", "Dung dịch và dịch truyền điều trị"),
                new CategorySeed("LOAI005", "Chẩn đoán", "Vật tư dùng cho xét nghiệm và chẩn đoán"),
                new CategorySeed("LOAI006", "Sát khuẩn", "Dung dịch sát khuẩn và làm sạch")
        );

        Set<String> existingCodes = new HashSet<>();
        supplyCategoryRepository.findAll().forEach(item -> existingCodes.add(item.getCode()));
        for (CategorySeed seed : seeds) {
            if (existingCodes.contains(seed.code())) {
                continue;
            }
            supplyCategoryRepository.save(new SupplyCategory(null, seed.code(), seed.name(), seed.description()));
        }
    }

    private void seedSuppliers() {
        List<SupplierSeed> seeds = List.of(
                new SupplierSeed("NCC001", "Công ty Thiết bị Y tế An Khang", "0909123456", "ankhang@demo.com", "12 Nguyễn Văn Linh, TP.HCM"),
                new SupplierSeed("NCC002", "Công ty Dược phẩm Minh Tâm", "0911222333", "minhtam@demo.com", "88 Hai Bà Trưng, Hà Nội"),
                new SupplierSeed("NCC003", "Công ty Vật tư Sức khỏe Việt", "0988333444", "suckhoeviet@demo.com", "25 Trần Phú, Đà Nẵng"),
                new SupplierSeed("NCC004", "Công ty Dược Hoàng Gia", "0908001122", "hoanggia@demo.com", "45 Lý Thường Kiệt, TP.HCM"),
                new SupplierSeed("NCC005", "Công ty Thiết bị Sao Nam", "0933004455", "saonam@demo.com", "116 Võ Văn Tần, TP.HCM"),
                new SupplierSeed("NCC006", "Công ty Chăm sóc Y tế Phúc An", "0966112233", "phucan@demo.com", "09 Hùng Vương, Cần Thơ")
        );

        Set<String> existingCodes = new HashSet<>();
        supplierRepository.findAll().forEach(item -> existingCodes.add(item.getCode()));
        for (SupplierSeed seed : seeds) {
            if (existingCodes.contains(seed.code())) {
                continue;
            }
            supplierRepository.save(new Supplier(null, seed.code(), seed.name(), seed.phone(), seed.email(), seed.address()));
        }
    }

    private void seedMedicalSupplies() {
        if (medicalSupplyRepository.count() >= TARGET_SUPPLIES) {
            return;
        }

        Map<String, SupplyCategory> categories = new HashMap<>();
        supplyCategoryRepository.findAll().forEach(item -> categories.put(item.getCode(), item));
        Map<String, Supplier> suppliers = new HashMap<>();
        supplierRepository.findAll().forEach(item -> suppliers.put(item.getCode(), item));

        List<SupplySeed> seeds = List.of(
                new SupplySeed("VT001", "Khẩu trang y tế 4 lớp", "LOAI002", 180, "Hộp", 30, 65000, 45, "NCC001"),
                new SupplySeed("VT002", "Găng tay y tế không bột", "LOAI002", 45, "Hộp", 50, 40000, 20, "NCC001"),
                new SupplySeed("VT003", "Thuốc Paracetamol 500mg", "LOAI001", 320, "Vỉ", 80, 15000, 150, "NCC002"),
                new SupplySeed("VT004", "Bông gòn tiệt trùng", "LOAI002", 25, "Gói", 40, 12000, 18, "NCC003"),
                new SupplySeed("VT005", "Nhiệt kế điện tử", "LOAI003", 12, "Cái", 10, 250000, 420, "NCC001"),
                new SupplySeed("VT006", "Dung dịch NaCl 0.9%", "LOAI004", 140, "Chai", 35, 18000, 120, "NCC004"),
                new SupplySeed("VT007", "Bơm tiêm 5ml", "LOAI002", 360, "Cái", 100, 3500, 90, "NCC005"),
                new SupplySeed("VT008", "Bơm tiêm insulin", "LOAI005", 90, "Cái", 30, 4200, 100, "NCC005"),
                new SupplySeed("VT009", "Cồn sát khuẩn 70 độ", "LOAI006", 70, "Chai", 25, 28000, 70, "NCC006"),
                new SupplySeed("VT010", "Kim luồn tĩnh mạch", "LOAI005", 58, "Cái", 25, 11000, 80, "NCC001"),
                new SupplySeed("VT011", "Băng cuộn y tế", "LOAI002", 150, "Cuộn", 40, 10000, 160, "NCC003"),
                new SupplySeed("VT012", "Máy đo SPO2 cầm tay", "LOAI003", 8, "Cái", 5, 650000, 540, "NCC004"),
                new SupplySeed("VT013", "Dung dịch sát khuẩn tay nhanh", "LOAI006", 44, "Chai", 25, 52000, 60, "NCC006"),
                new SupplySeed("VT014", "Que thử đường huyết", "LOAI005", 68, "Hộp", 20, 175000, 95, "NCC002"),
                new SupplySeed("VT015", "Thuốc Omeprazole 20mg", "LOAI001", 210, "Vỉ", 60, 21000, 130, "NCC002"),
                new SupplySeed("VT016", "Nước cất y tế", "LOAI004", 95, "Chai", 30, 9000, 75, "NCC004"),
                new SupplySeed("VT017", "Gạc vô khuẩn 10x10", "LOAI002", 135, "Gói", 45, 14500, 85, "NCC003"),
                new SupplySeed("VT018", "Bộ test cúm nhanh", "LOAI005", 28, "Bộ", 15, 92000, 35, "NCC005")
        );

        Set<String> existingCodes = new HashSet<>();
        medicalSupplyRepository.findAll().forEach(item -> existingCodes.add(item.getCode()));

        LocalDate today = LocalDate.now();
        for (SupplySeed seed : seeds) {
            if (existingCodes.contains(seed.code())) {
                continue;
            }

            MedicalSupply supply = new MedicalSupply();
            supply.setCode(seed.code());
            supply.setName(seed.name());
            supply.setCategory(categories.get(seed.categoryCode()));
            supply.setQuantity(seed.quantity());
            supply.setUnit(seed.unit());
            supply.setMinimumStock(seed.minimumStock());
            supply.setEstimatedUnitPrice(BigDecimal.valueOf(seed.price()));
            supply.setImportDate(today.minusDays(30 + existingCodes.size()));
            supply.setExpiryDate(today.plusDays(seed.expiryInDays()));
            supply.setSupplier(suppliers.get(seed.supplierCode()));
            supply.setQrCode("QR-" + seed.code());
            medicalSupplyRepository.save(supply);
        }
    }

    private void seedBatches() {
        if (supplyBatchRepository.count() >= 24) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<MedicalSupply> supplies = medicalSupplyRepository.findAll().stream()
                .sorted(Comparator.comparing(MedicalSupply::getCode))
                .toList();

        for (int i = 0; i < supplies.size(); i++) {
            MedicalSupply supply = supplies.get(i);
            if (supplyBatchRepository.existsByMedicalSupply_Id(supply.getId())) {
                continue;
            }

            SupplyBatch batch = new SupplyBatch();
            batch.setMedicalSupply(supply);
            batch.setBatchNumber("LO-" + supply.getCode() + "-01");
            batch.setManufactureDate(today.minusDays(60 + i));
            batch.setExpiryDate(supply.getExpiryDate());
            batch.setQuantity(Math.max(supply.getQuantity(), 0));
            supplyBatchRepository.save(batch);
        }
    }

    private void seedInboundReceipts() {
        long currentCount = inboundReceiptRepository.count();
        if (currentCount >= TARGET_INBOUND_RECEIPTS) {
            return;
        }

        List<MedicalSupply> supplies = medicalSupplyRepository.findAll().stream()
                .sorted(Comparator.comparing(MedicalSupply::getCode))
                .toList();

        for (int i = (int) currentCount; i < TARGET_INBOUND_RECEIPTS; i++) {
            MedicalSupply supply = supplies.get(i % supplies.size());
            int quantity = 20 + (i % 5) * 10;
            BigDecimal unitPrice = supply.getEstimatedUnitPrice() != null ? supply.getEstimatedUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));

            InboundReceipt receipt = new InboundReceipt();
            receipt.setReceiptCode(String.format("PN%03d", i + 1));
            receipt.setReceiptDate(LocalDate.now().minusDays(45L - i));
            receipt.setCreatedBy(i % 2 == 0 ? "Nguyễn Minh Anh" : "Lê Quốc Bảo");
            receipt.setNote("Phiếu nhập demo số " + (i + 1));
            receipt.setTotalAmount(amount);

            InboundReceiptDetail detail = new InboundReceiptDetail();
            detail.setReceipt(receipt);
            detail.setMedicalSupply(supply);
            detail.setQuantity(quantity);
            detail.setUnitPrice(unitPrice);
            detail.setAmount(amount);
            detail.setBatchNumber("AUTO-" + supply.getCode() + "-" + String.format("%02d", i + 1));
            detail.setManufactureDate(LocalDate.now().minusDays(90L + i));
            detail.setBatchExpiryDate(supply.getExpiryDate());

            receipt.getDetails().add(detail);
            inboundReceiptRepository.save(receipt);
        }
    }

    private void seedOutboundReceipts() {
        long currentCount = outboundReceiptRepository.count();
        if (currentCount >= TARGET_OUTBOUND_RECEIPTS) {
            return;
        }

        List<MedicalSupply> supplies = medicalSupplyRepository.findAll().stream()
                .sorted(Comparator.comparing(MedicalSupply::getCode))
                .toList();
        List<String> departments = List.of(
                "Khoa Cấp cứu", "Khoa Nội tổng quát", "Khoa Hô hấp", "Khoa Tim mạch",
                "Khoa Tiêu hóa", "Khoa Nhi", "Khoa Ngoại trú", "Khoa ICU"
        );

        for (int i = (int) currentCount; i < TARGET_OUTBOUND_RECEIPTS; i++) {
            MedicalSupply supply = supplies.get(i % supplies.size());
            int quantity = 5 + (i % 4) * 5;
            BigDecimal unitPrice = supply.getEstimatedUnitPrice() != null ? supply.getEstimatedUnitPrice() : BigDecimal.ZERO;
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OutboundReceipt receipt = new OutboundReceipt();
            receipt.setReceiptCode(String.format("PX%03d", i + 1));
            receipt.setReceiptDate(LocalDate.now().minusDays(30L - i));
            receipt.setCreatedBy(i % 2 == 0 ? "Trần Hoàng Nam" : "Phạm Thu Hà");
            receipt.setDepartmentName(departments.get(i % departments.size()));
            receipt.setNote("Phiếu xuất demo số " + (i + 1));
            receipt.setTotalAmount(amount);

            OutboundReceiptDetail detail = new OutboundReceiptDetail();
            detail.setReceipt(receipt);
            detail.setMedicalSupply(supply);
            detail.setQuantity(quantity);
            detail.setUnitPrice(unitPrice);
            detail.setAmount(amount);
            detail.setAllocatedBatches("LO-" + supply.getCode() + "-01 x " + quantity);

            receipt.getDetails().add(detail);
            outboundReceiptRepository.save(receipt);
        }
    }

    private void seedAlerts() {
        long currentCount = alertNotificationRepository.count();
        if (currentCount >= TARGET_ALERTS) {
            return;
        }

        List<MedicalSupply> supplies = medicalSupplyRepository.findAll().stream()
                .sorted(Comparator.comparing(MedicalSupply::getCode))
                .toList();
        AlertType[] types = AlertType.values();

        for (int i = (int) currentCount; i < TARGET_ALERTS; i++) {
            MedicalSupply supply = supplies.get(i % supplies.size());
            AlertType type = types[i % types.length];

            AlertNotification alert = new AlertNotification();
            alert.setAlertType(type);
            alert.setReferenceCode(type == AlertType.EXPIRING_BATCH ? supply.getCode() + "-LO-01" : supply.getCode());
            alert.setTitle(buildAlertTitle(type));
            alert.setMessage(buildAlertMessage(type, supply));
            alert.setResolved(false);
            alert.setCreatedAt(LocalDateTime.now().minusHours(i + 1));
            alert.setLastDetectedAt(LocalDateTime.now().minusMinutes((i + 1) * 15L));
            alertNotificationRepository.save(alert);
        }
    }

    private void seedAuditLogs() {
        long currentCount = auditLogRepository.count();
        if (currentCount >= TARGET_AUDIT_LOGS) {
            return;
        }

        List<MedicalSupply> supplies = medicalSupplyRepository.findAll().stream()
                .sorted(Comparator.comparing(MedicalSupply::getCode))
                .toList();
        String[] usernames = {"admin", "staff", "ketoan"};
        String[] actions = {"LOGIN_SUCCESS", "CREATE_SUPPLY", "UPDATE_SUPPLY", "CREATE_INBOUND_RECEIPT", "CREATE_OUTBOUND_RECEIPT"};

        for (int i = (int) currentCount; i < TARGET_AUDIT_LOGS; i++) {
            MedicalSupply supply = supplies.get(i % supplies.size());
            String action = actions[i % actions.length];

            AuditLog log = new AuditLog();
            log.setUsername(usernames[i % usernames.length]);
            log.setAction(action);
            log.setTargetType(action.contains("RECEIPT") ? "RECEIPT" : "MEDICAL_SUPPLY");
            log.setTargetValue(action.contains("INBOUND") ? String.format("PN%03d", i + 1)
                    : action.contains("OUTBOUND") ? String.format("PX%03d", i + 1)
                    : supply.getCode());
            log.setDescription(buildAuditDescription(action, supply));
            log.setCreatedAt(LocalDateTime.now().minusHours(i * 3L + 1));
            auditLogRepository.save(log);
        }
    }

    private String buildAlertTitle(AlertType type) {
        return switch (type) {
            case LOW_STOCK -> "Vật tư dưới mức tồn kho";
            case EXPIRING_BATCH -> "Lô vật tư sắp hết hạn";
            case CONSUMPTION_RISK -> "Nguy cơ tiêu hao tăng";
            case UNUSUAL_OUTBOUND -> "Phiếu xuất có biến động bất thường";
            case APPOINTMENT_REQUEST -> "Lịch khám mới từ cổng bệnh nhân";
            case APPOINTMENT_REMINDER -> "Lịch khám cần nhắc trong ngày mai";
        };
    }

    private String buildAlertMessage(AlertType type, MedicalSupply supply) {
        return switch (type) {
            case LOW_STOCK -> supply.getName() + " đang dưới hoặc gần mức tồn tối thiểu.";
            case EXPIRING_BATCH -> "Lô của " + supply.getName() + " đang tiến gần hạn dùng, cần ưu tiên FEFO.";
            case CONSUMPTION_RISK -> supply.getName() + " có xu hướng tiêu hao nhanh hơn trung bình 7 ngày gần đây.";
            case UNUSUAL_OUTBOUND -> "Xuất kho " + supply.getName() + " tăng bất thường so với tuần trước.";
            case APPOINTMENT_REQUEST -> "Bệnh nhân vừa tạo lịch khám mới từ cổng bệnh nhân.";
            case APPOINTMENT_REMINDER -> "Có lịch khám ngày mai cần chủ động nhắc và chuẩn bị tiếp nhận.";
        };
    }

    private String buildAuditDescription(String action, MedicalSupply supply) {
        return switch (action) {
            case "LOGIN_SUCCESS" -> "Người dùng đăng nhập thành công vào hệ thống.";
            case "CREATE_SUPPLY" -> "Thêm mới vật tư " + supply.getName() + ".";
            case "UPDATE_SUPPLY" -> "Cập nhật thông tin vật tư " + supply.getName() + ".";
            case "CREATE_INBOUND_RECEIPT" -> "Tạo phiếu nhập kho có liên quan đến vật tư " + supply.getCode() + ".";
            case "CREATE_OUTBOUND_RECEIPT" -> "Tạo phiếu xuất kho có liên quan đến vật tư " + supply.getCode() + ".";
            default -> "Ghi nhận thao tác hệ thống.";
        };
    }

    private record CategorySeed(String code, String name, String description) { }

    private record SupplierSeed(String code, String name, String phone, String email, String address) { }

    private record DepartmentSeed(
            String code,
            String name,
            String location,
            String workingHours,
            String hotline,
            String description
    ) { }

    private record ServicePriceSeed(
            String code,
            String serviceName,
            long minPrice,
            long maxPrice,
            String note
    ) { }

    private record ScreeningPackageSeed(
            String code,
            String name,
            String targetGroup,
            String includedServices,
            long price,
            String note
    ) { }

    private record DepartmentSupplySeed(
            String department,
            String supplyCode,
            String reason,
            int priority
    ) { }

    private record SupplySeed(
            String code,
            String name,
            String categoryCode,
            int quantity,
            String unit,
            int minimumStock,
            long price,
            long expiryInDays,
            String supplierCode
    ) { }
}
