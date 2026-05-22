package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssistantResponse {

    private final String type;
    private final String department;

    @JsonProperty("risk_score")
    private final int riskScore;

    @JsonProperty("risk_level")
    private final String riskLevel;

    private final String answer;
    private final String advice;
    private final boolean emergency;
}
