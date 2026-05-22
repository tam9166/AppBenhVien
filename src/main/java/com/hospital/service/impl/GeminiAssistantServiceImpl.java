package com.hospital.service.impl;

import com.hospital.config.AssistantProperties;
import com.hospital.service.GeminiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeminiAssistantServiceImpl implements GeminiAssistantService {

    private final AssistantProperties assistantProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public Optional<String> generateTriageNote(String message, String ruleBasedSummary) {
        AssistantProperties.Gemini gemini = assistantProperties.getGemini();
        if (!gemini.isEnabled() || !StringUtils.hasText(gemini.getApiKey())) {
            return Optional.empty();
        }

        String url = String.format("%s/models/%s:generateContent?key=%s",
                gemini.getEndpoint(), gemini.getModel(), gemini.getApiKey());
        String prompt = """
                Bạn là trợ lý điều hướng bệnh nhân của bệnh viện.
                Không chẩn đoán bệnh, không kê thuốc, không thay thế bác sĩ.
                Chỉ viết một ghi chú ngắn để làm rõ hướng dẫn sàng lọc dựa trên kết quả rule-based.

                Câu hỏi bệnh nhân: %s
                Kết quả rule-based: %s
                """.formatted(message, ruleBasedSummary);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            Map<?, ?> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            return extractText(response);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<String> extractText(Map<?, ?> response) {
        if (response == null) {
            return Optional.empty();
        }
        Object candidatesObject = response.get("candidates");
        if (!(candidatesObject instanceof List<?> candidates) || candidates.isEmpty()) {
            return Optional.empty();
        }
        Object firstCandidate = candidates.get(0);
        if (!(firstCandidate instanceof Map<?, ?> candidate)) {
            return Optional.empty();
        }
        Object contentObject = candidate.get("content");
        if (!(contentObject instanceof Map<?, ?> content)) {
            return Optional.empty();
        }
        Object partsObject = content.get("parts");
        if (!(partsObject instanceof List<?> parts) || parts.isEmpty()) {
            return Optional.empty();
        }
        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> part)) {
            return Optional.empty();
        }
        Object text = part.get("text");
        return text instanceof String value && StringUtils.hasText(value)
                ? Optional.of(value.trim())
                : Optional.empty();
    }
}
