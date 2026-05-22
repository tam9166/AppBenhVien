package com.hospital.service;

import java.util.Optional;

public interface GeminiAssistantService {

    Optional<String> generateTriageNote(String message, String ruleBasedSummary);
}
