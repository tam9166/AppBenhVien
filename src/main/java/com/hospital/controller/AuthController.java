package com.hospital.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "He thong quan ly vat tu y te");
        model.addAttribute("mobileScanUrl", appBaseUrl + "/qr/scan-live");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
