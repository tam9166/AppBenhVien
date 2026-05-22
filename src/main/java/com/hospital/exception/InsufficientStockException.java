package com.hospital.exception;

/**
 * Ngoại lệ khi xuất kho vượt quá số lượng hiện có.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
