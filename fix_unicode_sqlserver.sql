ALTER TABLE audit_logs ALTER COLUMN username NVARCHAR(50) NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN action NVARCHAR(100) NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN target_type NVARCHAR(50) NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN target_value NVARCHAR(150) NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN description NVARCHAR(500) NOT NULL;

ALTER TABLE alert_notifications ALTER COLUMN reference_code NVARCHAR(50) NOT NULL;
ALTER TABLE alert_notifications ALTER COLUMN title NVARCHAR(150) NOT NULL;
ALTER TABLE alert_notifications ALTER COLUMN message NVARCHAR(500) NOT NULL;

ALTER TABLE vat_tu ALTER COLUMN ma_vat_tu NVARCHAR(30) NOT NULL;
ALTER TABLE vat_tu ALTER COLUMN ten_vat_tu NVARCHAR(150) NOT NULL;
ALTER TABLE vat_tu ALTER COLUMN don_vi NVARCHAR(50) NOT NULL;
ALTER TABLE vat_tu ALTER COLUMN ma_qr NVARCHAR(100) NOT NULL;
ALTER TABLE vat_tu ALTER COLUMN trang_thai NVARCHAR(30) NOT NULL;

ALTER TABLE nha_cung_cap ALTER COLUMN ma_nha_cung_cap NVARCHAR(30) NOT NULL;
ALTER TABLE nha_cung_cap ALTER COLUMN ten_nha_cung_cap NVARCHAR(150) NOT NULL;
ALTER TABLE nha_cung_cap ALTER COLUMN so_dien_thoai NVARCHAR(20) NULL;
ALTER TABLE nha_cung_cap ALTER COLUMN email NVARCHAR(100) NULL;
ALTER TABLE nha_cung_cap ALTER COLUMN dia_chi NVARCHAR(255) NULL;

UPDATE audit_logs
SET description = N'Người dùng đăng nhập thành công vào hệ thống.'
WHERE action = N'LOGIN_SUCCESS';

UPDATE vat_tu
SET ten_vat_tu = N'Găng tay y tế không bột', don_vi = N'Hộp'
WHERE ma_vat_tu = N'VT002';

UPDATE vat_tu
SET ten_vat_tu = N'Bông gòn tiệt trùng', don_vi = N'Gói'
WHERE ma_vat_tu = N'VT004';

UPDATE alert_notifications
SET title = N'Vật tư dưới mức tồn kho',
    message = N'Vật tư Găng tay y tế không bột chỉ còn 45 Hộp, thấp hơn mức tối thiểu 50.'
WHERE reference_code = N'VT002';

UPDATE alert_notifications
SET title = N'Lô vật tư sắp hết hạn',
    message = N'Lô LO-BG-002 của Bông gòn tiệt trùng sắp hết hạn.'
WHERE reference_code = N'VT004-LO-BG-002';
