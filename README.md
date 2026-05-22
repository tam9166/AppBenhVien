# He thong quan ly vat tu y te va cong benh nhan

Du an Spring Boot MVC cho benh vien, gom quan ly vat tu y te bang QR Code, cong benh nhan, dat lich kham, theo doi lich hen va chatbot AI ho tro sang loc/tu van dieu huong.

## Cong nghe su dung

- Java 17
- Spring Boot 3.3.5
- Spring Data JPA
- Spring Security
- Thymeleaf
- SQL Server
- Bootstrap 5
- ZXing QR Code
- Apache POI
- OpenPDF
- OpenAI/Gemini API tuy chon cho chatbot

## Tai khoan mau

- `admin / 123456`
- `staff / 123456`

Nen doi mat khau mau neu dua he thong len moi truong that.

## Cau hinh SQL Server

Ung dung doc cau hinh qua bien moi truong:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=medical_inventory_management;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=your_password
```

Neu chay local bang profile `local`, cau hinh nam tai:

```text
src/main/resources/application-local.properties
```

File `database.sql` co the dung de tao du lieu ban dau khi can.

## Cau hinh chatbot AI

Mac dinh chatbot co the tra loi theo tap luat/noi dung san co. Neu muon bat AI:

```properties
OPENAI_ENABLED=true
OPENAI_API_KEY=your_openai_api_key
OPENAI_MODEL=gpt-4o-mini
```

Hoac dung Gemini:

```properties
GEMINI_API_KEY=your_gemini_api_key
```

Khong dua API key that vao GitHub. Hay dung bien moi truong tren may chay ung dung.

## Chay project

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Sau khi ung dung khoi dong, truy cap:

```text
http://localhost:8080/login
```

Neu cong `8080` dang bi dung, hay tat tien trinh cu hoac doi `server.port`.

## Ghi chu

- Thu muc `target/` va cac file log da duoc bo qua bang `.gitignore`.
- Khong commit file `.env` hoac thong tin mat khau/API key that.
- Neu gap loi ket noi SQL Server, hay kiem tra SQL Server dang chay va TCP/IP port `1433` da mo.
