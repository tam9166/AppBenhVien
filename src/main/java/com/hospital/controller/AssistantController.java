package com.hospital.controller;

import com.hospital.dto.AssistantResponse;
import com.hospital.service.HospitalAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final HospitalAssistantService hospitalAssistantService;

    @GetMapping
    public AssistantResponse analyze(@RequestParam(defaultValue = "") String message, Authentication authentication) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return hospitalAssistantService.analyze(message, authenticated);
    }
}
