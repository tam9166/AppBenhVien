package com.hospital.controller;

import com.hospital.config.AssistantProperties;
import com.hospital.dto.AssistantResponse;
import com.hospital.service.HospitalAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook/facebook")
@RequiredArgsConstructor
public class FacebookWebhookController {

    private final AssistantProperties assistantProperties;
    private final HospitalAssistantService hospitalAssistantService;

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        boolean valid = assistantProperties.getFacebook().isEnabled()
                && "subscribe".equals(mode)
                && StringUtils.hasText(verifyToken)
                && verifyToken.equals(assistantProperties.getFacebook().getVerifyToken());
        return valid ? ResponseEntity.ok(challenge) : ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> receive(@RequestBody Map<String, Object> payload) {
        if (!assistantProperties.getFacebook().isEnabled()) {
            return ResponseEntity.status(403).body(Map.of("status", "disabled"));
        }

        String message = String.valueOf(payload.getOrDefault("message", ""));
        AssistantResponse response = hospitalAssistantService.analyze(message, false);
        return ResponseEntity.ok(Map.of(
                "status", "received",
                "reply", response.getAnswer(),
                "advice", response.getAdvice(),
                "department", response.getDepartment(),
                "emergency", response.isEmergency()
        ));
    }
}
