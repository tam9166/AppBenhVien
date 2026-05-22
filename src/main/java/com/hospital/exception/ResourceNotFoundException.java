package com.hospital.exception;

/**
 * Ngoại lệ dùng khi không tìm thấy dữ liệu theo id/code.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
