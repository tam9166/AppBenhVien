package com.hospital.controller;

import com.hospital.entity.MedicalSupply;
import com.hospital.service.MedicalSupplyService;
import com.hospital.utils.QrCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sinh và hiển thị thông tin QR Code cho vật tư.
 */
@Controller
@RequestMapping("/qr")
@RequiredArgsConstructor
public class QrController {

    private final MedicalSupplyService medicalSupplyService;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> generateQrImage(@PathVariable Long id) {
        MedicalSupply supply = medicalSupplyService.getById(id);
        String qrContent = appBaseUrl + "/scan/" + supply.getCode();
        byte[] image = qrCodeGenerator.generateQrCode(qrContent, 250, 250);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @GetMapping("/scan-live")
    public String showLiveScanner(Model model, HttpServletRequest request, Authentication authentication) {
        model.addAttribute("scanBaseUrl", appBaseUrl + "/scan/");
        model.addAttribute("mobileScanUrl", appBaseUrl + "/qr/scan-live");
        model.addAttribute("backUrl", resolveBackUrl(request, authentication));
        return "inventory/scan-live";
    }

    @GetMapping("/live-entry-image")
    public ResponseEntity<byte[]> generateLiveEntryQr() {
        byte[] image = qrCodeGenerator.generateQrCode(appBaseUrl + "/qr/scan-live", 280, 280);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    /**
     * Public path đẹp hơn để demo quét QR như hệ thống thật.
     */
    @GetMapping("/scan/{code}")
    public String showPublicSupplyInfo(@PathVariable String code, Model model) {
        model.addAttribute("supply", medicalSupplyService.getByCode(code));
        model.addAttribute("publicQrUrl", appBaseUrl + "/scan/" + code);
        return "inventory/qr-info";
    }

    @GetMapping("/info/{qrCode}")
    public String showQrInfo(@PathVariable String qrCode, Model model) {
        MedicalSupply supply = medicalSupplyService.getByQrCode(qrCode);
        model.addAttribute("supply", supply);
        model.addAttribute("publicQrUrl", appBaseUrl + "/scan/" + supply.getCode());
        return "inventory/qr-info";
    }

    private String resolveBackUrl(HttpServletRequest request, Authentication authentication) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer != null && !referer.isBlank()) {
            String currentUrl = request.getRequestURL().toString();
            if (!referer.equals(currentUrl) && !referer.endsWith("/qr/scan-live")) {
                return referer;
            }
        }

        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return authenticated ? "/dashboard" : "/";
    }
}
