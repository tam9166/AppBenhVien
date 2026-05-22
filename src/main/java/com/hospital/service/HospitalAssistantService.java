package com.hospital.service;

import com.hospital.dto.AssistantResponse;

public interface HospitalAssistantService {

    AssistantResponse analyze(String message, boolean authenticated);
}
