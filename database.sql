/*
    File khoi tao database SQL Server cho de tai:
    HE THONG QUAN LY VAT TU Y TE BANG QR CODE
*/

IF DB_ID('medical_inventory_management') IS NULL
BEGIN
    CREATE DATABASE medical_inventory_management;
END
GO

IF OBJECT_ID('department_infos', 'U') IS NULL
CREATE TABLE department_infos (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(100) NOT NULL,
    location NVARCHAR(150) NOT NULL,
    working_hours NVARCHAR(150) NOT NULL,
    hotline NVARCHAR(30),
    description NVARCHAR(500)
);
GO

IF OBJECT_ID('medical_service_prices', 'U') IS NULL
CREATE TABLE medical_service_prices (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    service_name NVARCHAR(150) NOT NULL,
    min_price DECIMAL(18,2) NOT NULL,
    max_price DECIMAL(18,2),
    note NVARCHAR(500)
);
GO

IF OBJECT_ID('chatbot_conversation_logs', 'U') IS NULL
CREATE TABLE chatbot_conversation_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_message NVARCHAR(MAX) NOT NULL,
    response_type NVARCHAR(30) NOT NULL,
    department NVARCHAR(100),
    risk_score INT NOT NULL,
    risk_level NVARCHAR(30) NOT NULL,
    emergency BIT NOT NULL,
    authenticated BIT NOT NULL,
    source NVARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

USE medical_inventory_management;
GO

IF OBJECT_ID('chi_tiet_phieu_xuat', 'U') IS NOT NULL DROP TABLE chi_tiet_phieu_xuat;
IF OBJECT_ID('phieu_xuat', 'U') IS NOT NULL DROP TABLE phieu_xuat;
IF OBJECT_ID('chi_tiet_phieu_nhap', 'U') IS NOT NULL DROP TABLE chi_tiet_phieu_nhap;
IF OBJECT_ID('phieu_nhap', 'U') IS NOT NULL DROP TABLE phieu_nhap;
IF OBJECT_ID('supply_batches', 'U') IS NOT NULL DROP TABLE supply_batches;
IF OBJECT_ID('alert_notifications', 'U') IS NOT NULL DROP TABLE alert_notifications;
IF OBJECT_ID('audit_logs', 'U') IS NOT NULL DROP TABLE audit_logs;
IF OBJECT_ID('vat_tu', 'U') IS NOT NULL DROP TABLE vat_tu;
IF OBJECT_ID('user_roles', 'U') IS NOT NULL DROP TABLE user_roles;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('nha_cung_cap', 'U') IS NOT NULL DROP TABLE nha_cung_cap;
IF OBJECT_ID('loai_vat_tu', 'U') IS NOT NULL DROP TABLE loai_vat_tu;
GO

CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255)
);
GO

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    enabled BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
GO

CREATE TABLE loai_vat_tu (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_loai NVARCHAR(30) NOT NULL UNIQUE,
    ten_loai NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(255)
);
GO

CREATE TABLE nha_cung_cap (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nha_cung_cap NVARCHAR(30) NOT NULL UNIQUE,
    ten_nha_cung_cap NVARCHAR(150) NOT NULL,
    so_dien_thoai NVARCHAR(20),
    email NVARCHAR(100),
    dia_chi NVARCHAR(255)
);
GO

CREATE TABLE vat_tu (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_vat_tu NVARCHAR(30) NOT NULL UNIQUE,
    ten_vat_tu NVARCHAR(150) NOT NULL,
    loai_vat_tu_id BIGINT NOT NULL,
    so_luong INT NOT NULL DEFAULT 0,
    don_vi NVARCHAR(50) NOT NULL,
    muc_ton_toi_thieu INT NOT NULL DEFAULT 10,
    don_gia_uoc_tinh DECIMAL(18,2) NOT NULL DEFAULT 0,
    ngay_nhap DATE NOT NULL,
    ngay_het_han DATE NOT NULL,
    nha_cung_cap_id BIGINT NOT NULL,
    ma_qr NVARCHAR(100) NOT NULL UNIQUE,
    trang_thai NVARCHAR(30) NOT NULL,
    CONSTRAINT fk_vat_tu_loai FOREIGN KEY (loai_vat_tu_id) REFERENCES loai_vat_tu(id),
    CONSTRAINT fk_vat_tu_ncc FOREIGN KEY (nha_cung_cap_id) REFERENCES nha_cung_cap(id)
);
GO

CREATE TABLE phieu_nhap (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_nhap NVARCHAR(30) NOT NULL UNIQUE,
    ngay_nhap DATE NOT NULL,
    nguoi_nhap NVARCHAR(100) NOT NULL,
    ghi_chu NVARCHAR(255),
    tong_tien DECIMAL(18,2) DEFAULT 0
);
GO

CREATE TABLE supply_batches (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    medical_supply_id BIGINT NOT NULL,
    batch_number NVARCHAR(50) NOT NULL,
    manufacture_date DATE NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_supply_batches_supply FOREIGN KEY (medical_supply_id) REFERENCES vat_tu(id)
);
GO

CREATE TABLE chi_tiet_phieu_nhap (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    phieu_nhap_id BIGINT NOT NULL,
    vat_tu_id BIGINT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    thanh_tien DECIMAL(18,2) NOT NULL,
    so_lo NVARCHAR(50) NOT NULL,
    ngay_san_xuat DATE NULL,
    han_su_dung_lo DATE NOT NULL,
    CONSTRAINT fk_ctpn_phieu_nhap FOREIGN KEY (phieu_nhap_id) REFERENCES phieu_nhap(id),
    CONSTRAINT fk_ctpn_vat_tu FOREIGN KEY (vat_tu_id) REFERENCES vat_tu(id)
);
GO

CREATE TABLE phieu_xuat (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_xuat NVARCHAR(30) NOT NULL UNIQUE,
    ngay_xuat DATE NOT NULL,
    nguoi_xuat NVARCHAR(100) NOT NULL,
    khoa_nhan NVARCHAR(100),
    ghi_chu NVARCHAR(255),
    tong_tien DECIMAL(18,2) DEFAULT 0
);
GO

CREATE TABLE chi_tiet_phieu_xuat (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    phieu_xuat_id BIGINT NOT NULL,
    vat_tu_id BIGINT NOT NULL,
    so_luong INT NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    thanh_tien DECIMAL(18,2) NOT NULL,
    allocated_batches NVARCHAR(255) NULL,
    CONSTRAINT fk_ctpx_phieu_xuat FOREIGN KEY (phieu_xuat_id) REFERENCES phieu_xuat(id),
    CONSTRAINT fk_ctpx_vat_tu FOREIGN KEY (vat_tu_id) REFERENCES vat_tu(id)
);
GO

CREATE TABLE audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL,
    action NVARCHAR(100) NOT NULL,
    target_type NVARCHAR(50) NOT NULL,
    target_value NVARCHAR(150) NOT NULL,
    description NVARCHAR(500) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE alert_notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    alert_type NVARCHAR(30) NOT NULL,
    reference_code NVARCHAR(50) NOT NULL,
    title NVARCHAR(150) NOT NULL,
    message NVARCHAR(500) NOT NULL,
    resolved BIT NOT NULL DEFAULT 0,
    last_detected_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CK_alert_notifications_alert_type CHECK (
        alert_type IN (N'LOW_STOCK', N'EXPIRING_BATCH', N'CONSUMPTION_RISK', N'UNUSUAL_OUTBOUND')
    )
);
GO

CREATE INDEX idx_vat_tu_ten ON vat_tu(ten_vat_tu);
CREATE INDEX idx_vat_tu_han ON vat_tu(ngay_het_han);
CREATE INDEX idx_phieu_nhap_ngay ON phieu_nhap(ngay_nhap);
CREATE INDEX idx_phieu_xuat_ngay ON phieu_xuat(ngay_xuat);
CREATE INDEX idx_supply_batches_supply ON supply_batches(medical_supply_id, expiry_date);
CREATE INDEX idx_alert_notifications_active ON alert_notifications(resolved, alert_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
GO

INSERT INTO roles(name, description) VALUES
(N'ROLE_ADMIN', N'Quản trị toàn bộ hệ thống'),
(N'ROLE_STAFF', N'Nhân viên nhập xuất kho');
GO

INSERT INTO users(username, password, full_name, email, enabled) VALUES
(N'admin', N'123456', N'Quản trị viên hệ thống', N'admin@hospital.com', 1),
(N'staff', N'123456', N'Nhân viên kho', N'staff@hospital.com', 1);
GO

INSERT INTO user_roles(user_id, role_id) VALUES
(1, 1),
(2, 2);
GO

INSERT INTO loai_vat_tu(ma_loai, ten_loai, mo_ta) VALUES
(N'LOAI001', N'Thuốc', N'Nhóm thuốc điều trị và thuốc cấp cứu'),
(N'LOAI002', N'Vật tư tiêu hao', N'Bông, gạc, khẩu trang, găng tay'),
(N'LOAI003', N'Thiết bị nhỏ', N'Dụng cụ y tế kích thước nhỏ');
GO

INSERT INTO nha_cung_cap(ma_nha_cung_cap, ten_nha_cung_cap, so_dien_thoai, email, dia_chi) VALUES
(N'NCC001', N'Công ty Thiết bị Y tế An Khang', N'0909123456', N'ankhang@demo.com', N'12 Nguyễn Văn Linh, TP.HCM'),
(N'NCC002', N'Công ty Dược phẩm Minh Tâm', N'0911222333', N'minhtam@demo.com', N'88 Hai Bà Trưng, Hà Nội'),
(N'NCC003', N'Công ty Vật tư Sức Khỏe Việt', N'0988333444', N'suckhoeviet@demo.com', N'25 Trần Phú, Đà Nẵng');
GO

INSERT INTO vat_tu(ma_vat_tu, ten_vat_tu, loai_vat_tu_id, so_luong, don_vi, muc_ton_toi_thieu, don_gia_uoc_tinh, ngay_nhap, ngay_het_han, nha_cung_cap_id, ma_qr, trang_thai) VALUES
(N'VT001', N'Khẩu trang y tế 4 lớp', 2, 180, N'Hộp', 30, 65000, '2026-04-01', '2027-03-30', 1, N'QR-VT001', N'AVAILABLE'),
(N'VT002', N'Găng tay y tế không bột', 2, 45, N'Hộp', 50, 40000, '2026-04-10', '2027-02-28', 1, N'QR-VT002', N'LOW_STOCK'),
(N'VT003', N'Thuốc Paracetamol 500mg', 1, 320, N'Vỉ', 80, 15000, '2026-03-12', '2026-11-20', 2, N'QR-VT003', N'AVAILABLE'),
(N'VT004', N'Bông gòn tiệt trùng', 2, 25, N'Gói', 40, 12000, '2026-05-01', '2026-06-05', 3, N'QR-VT004', N'EXPIRING_SOON'),
(N'VT005', N'Nhiệt kế điện tử', 3, 12, N'Cái', 10, 250000, '2026-02-18', '2028-02-18', 1, N'QR-VT005', N'AVAILABLE');
GO

INSERT INTO phieu_nhap(ma_phieu_nhap, ngay_nhap, nguoi_nhap, ghi_chu, tong_tien) VALUES
(N'PN001', '2026-04-01', N'Nguyễn Minh Anh', N'Nhập kho đầu tháng 4', 15000000),
(N'PN002', '2026-05-01', N'Lê Quốc Bảo', N'Bổ sung vật tư tiêu hao', 9800000);
GO

INSERT INTO chi_tiet_phieu_nhap(phieu_nhap_id, vat_tu_id, so_luong, don_gia, thanh_tien, so_lo, ngay_san_xuat, han_su_dung_lo) VALUES
(1, 1, 100, 65000, 6500000, N'LO-KT-001', '2026-03-01', '2027-03-30'),
(1, 3, 200, 15000, 3000000, N'LO-TH-001', '2026-02-10', '2026-11-20'),
(2, 2, 80, 40000, 3200000, N'LO-GT-001', '2026-04-01', '2027-02-28'),
(2, 4, 50, 12000, 600000, N'LO-BG-001', '2026-04-20', '2026-06-05');
GO

INSERT INTO phieu_xuat(ma_phieu_xuat, ngay_xuat, nguoi_xuat, khoa_nhan, ghi_chu, tong_tien) VALUES
(N'PX001', '2026-04-15', N'Nguyễn Minh Anh', N'Khoa cấp cứu', N'Cấp vật tư ca trực', 2400000),
(N'PX002', '2026-05-08', N'Lê Quốc Bảo', N'Khoa nội tổng hợp', N'Sử dụng điều trị nội trú', 1850000);
GO

INSERT INTO chi_tiet_phieu_xuat(phieu_xuat_id, vat_tu_id, so_luong, don_gia, thanh_tien, allocated_batches) VALUES
(1, 1, 20, 65000, 1300000, N'LO-KT-001 x 20'),
(1, 2, 10, 40000, 400000, N'LO-GT-001 x 10'),
(2, 3, 30, 15000, 450000, N'LO-TH-001 x 30'),
(2, 4, 50, 12000, 600000, N'LO-BG-001 x 50');
GO

INSERT INTO supply_batches(medical_supply_id, batch_number, manufacture_date, expiry_date, quantity) VALUES
(1, N'LO-KT-001', '2026-03-01', '2027-03-30', 80),
(1, N'LO-KT-002', '2026-04-15', '2027-04-15', 100),
(2, N'LO-GT-001', '2026-04-01', '2027-02-28', 70),
(3, N'LO-TH-001', '2026-02-10', '2026-11-20', 170),
(3, N'LO-TH-002', '2026-03-25', '2026-12-31', 150),
(4, N'LO-BG-001', '2026-04-20', '2026-06-05', 0),
(4, N'LO-BG-002', '2026-05-05', '2026-06-25', 25),
(5, N'LO-NK-001', '2026-02-18', '2028-02-18', 12);
GO

INSERT INTO alert_notifications(alert_type, reference_code, title, message, resolved) VALUES
(N'LOW_STOCK', N'VT002', N'Vật tư dưới mức tồn kho', N'Găng tay y tế không bột đang dưới mức tồn tối thiểu.', 0),
(N'EXPIRING_BATCH', N'VT004-LO-BG-002', N'Lô vật tư sắp hết hạn', N'Lô LO-BG-002 của Bông gòn tiệt trùng sắp hết hạn.', 0);
GO

INSERT INTO audit_logs(username, action, target_type, target_value, description) VALUES
(N'admin', N'LOGIN_SUCCESS', N'USER', N'admin', N'Người dùng đăng nhập thành công vào hệ thống.'),
(N'admin', N'CREATE_SUPPLY', N'MEDICAL_SUPPLY', N'VT001', N'Thêm mới vật tư Khẩu trang y tế 4 lớp.'),
(N'staff', N'CREATE_INBOUND_RECEIPT', N'INBOUND_RECEIPT', N'PN001', N'Tạo phiếu nhập đầu kỳ với các lô vật tư.'),
(N'staff', N'CREATE_OUTBOUND_RECEIPT', N'OUTBOUND_RECEIPT', N'PX001', N'Tạo phiếu xuất cho khoa cấp cứu.');
GO
