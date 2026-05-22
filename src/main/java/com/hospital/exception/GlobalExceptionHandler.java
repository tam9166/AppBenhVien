package com.hospital.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Bắt lỗi tập trung để giao diện hiển thị thân thiện hơn.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException exception, Model model, HttpServletRequest request) {
        model.addAttribute("errorTitle", "Không tìm thấy dữ liệu");
        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }

    @ExceptionHandler(InsufficientStockException.class)
    public String handleInsufficientStock(InsufficientStockException exception, Model model, HttpServletRequest request) {
        model.addAttribute("errorTitle", "Không đủ tồn kho");
        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException exception, Model model, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ.");
        model.addAttribute("errorTitle", "Lỗi kiểm tra dữ liệu");
        model.addAttribute("errorMessage", message);
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception exception, Model model, HttpServletRequest request) {
        model.addAttribute("errorTitle", "Hệ thống gặp sự cố");
        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/general-error";
    }
}
