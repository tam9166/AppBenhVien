package com.hospital.service.impl;

import com.hospital.config.AssistantProperties;
import com.hospital.service.OpenAiAssistantService;
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
public class OpenAiAssistantServiceImpl implements OpenAiAssistantService {

    private final AssistantProperties assistantProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public Optional<String> generateTriageNote(String message, String ruleBasedSummary) {
        AssistantProperties.Openai openai = assistantProperties.getOpenai();
        if (!openai.isEnabled() || !StringUtils.hasText(openai.getApiKey())) {
            return Optional.empty();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openai.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
                Bạn là trợ lý điều hướng bệnh nhân của bệnh viện.
                Không chẩn đoán bệnh, không kê thuốc, không thay thế bác sĩ.
                Hãy viết 2-3 câu tiếng Việt tự nhiên, rõ ràng, thân thiện để:
                - tóm tắt nhanh mức độ ưu tiên
                - nhắc bệnh nhân cần theo dõi dấu hiệu gì
                - khuyến nghị đi đúng khoa đã được gợi ý

                Câu hỏi bệnh nhân: %s
                Kết quả rule-based: %s
                """.formatted(message, ruleBasedSummary);

        Map<String, Object> payload = Map.of(
                "model", openai.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "Bạn là trợ lý sàng lọc sơ bộ cho bệnh viện."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            Map<?, ?> response = restTemplate.postForObject(
                    openai.getEndpoint(),
                    new HttpEntity<>(payload, headers),
                    Map.class
            );
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
        Object choicesObject = response.get("choices");
        if (!(choicesObject instanceof List<?> choices) || choices.isEmpty()) {
            return Optional.empty();
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)) {
            return Optional.empty();
        }
        Object messageObject = choice.get("message");
        if (!(messageObject instanceof Map<?, ?> message)) {
            return Optional.empty();
        }
        Object content = message.get("content");
        return content instanceof String text && StringUtils.hasText(text)
                ? Optional.of(text.trim())
                : Optional.empty();
    }
}
