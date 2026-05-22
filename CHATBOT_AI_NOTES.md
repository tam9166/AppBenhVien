# Chatbot AI hỗ trợ sàng lọc và điều hướng bệnh nhân

## Phạm vi hiện tại

Ứng dụng đã triển khai chatbot trên website qua endpoint `/api/assistant`. Phiên bản mặc định dùng rule-based NLP để demo ổn định: chuẩn hóa tiếng Việt không dấu, nhận diện intent bằng nhóm từ khóa, tính điểm nguy cơ và gợi ý khoa phòng.

## Luồng xử lý

1. Chuẩn hóa câu hỏi: bỏ dấu, chuyển chữ thường, loại ký tự đặc biệt.
2. Nhận diện nhóm intent:
   - FAQ: giờ khám, địa chỉ, hotline, danh sách khoa, chi phí.
   - TRIAGE: triệu chứng sơ bộ để gợi ý khoa.
   - EMERGENCY: dấu hiệu nguy hiểm cần cấp cứu.
   - INVENTORY: chỉ dùng cho người đã đăng nhập để tra cứu kho nội bộ.
3. Tính `risk_score` theo triệu chứng nguy cơ như khó thở, đau ngực, co giật, mất ý thức, yếu/nói khó/méo miệng.
4. Trả về khoa gợi ý, mức độ nguy cơ, câu trả lời và hướng dẫn.
5. Lưu lịch sử vào bảng `chatbot_conversation_logs`.

## Dữ liệu quản trị

Các dữ liệu không nên hard-code lâu dài đã được tách thành bảng:

- `department_infos`: mã khoa, tên khoa, vị trí, giờ làm việc, hotline.
- `medical_service_prices`: bảng giá tham khảo.
- `chatbot_conversation_logs`: lịch sử câu hỏi và kết quả sàng lọc.

## Gemini API

Mặc định hệ thống không gọi Gemini để tránh phụ thuộc API key khi demo. Có thể bật bằng biến môi trường:

```properties
GEMINI_ENABLED=true
GEMINI_API_KEY=your_key
GEMINI_MODEL=gemini-2.0-flash
```

Khi bật, rule-based NLP vẫn là lớp quyết định chính; Gemini chỉ bổ sung ghi chú diễn đạt tự nhiên và không được phép chẩn đoán/kê thuốc.

## Facebook Messenger

Đã có webhook khung tại `/webhook/facebook`. Endpoint này phục vụ thiết kế tích hợp Messenger:

- `GET /webhook/facebook`: xác minh webhook bằng verify token.
- `POST /webhook/facebook`: nhận payload mẫu và trả kết quả chatbot.

Để chạy production cần bổ sung parse payload chuẩn của Meta, gửi reply qua Send API và bảo vệ token.

## Giới hạn y tế

Chatbot chỉ hỗ trợ sàng lọc ban đầu và điều hướng khoa phòng. Mọi câu trả lời phải giữ disclaimer: không thay thế bác sĩ, không chẩn đoán bệnh, không kê thuốc. Nếu có dấu hiệu cấp cứu, hệ thống ưu tiên hướng dẫn đến khoa Cấp cứu hoặc gọi hotline.
