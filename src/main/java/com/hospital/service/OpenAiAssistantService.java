package com.hospital.service;

import java.util.Optional;

public interface OpenAiAssistantService {

    Optional<String> generateTriageNote(String message, String ruleBasedSummary);
}
