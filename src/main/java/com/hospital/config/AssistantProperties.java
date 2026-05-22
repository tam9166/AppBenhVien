package com.hospital.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.assistant")
public class AssistantProperties {

    private Gemini gemini = new Gemini();
    private Openai openai = new Openai();
    private Facebook facebook = new Facebook();

    @Getter
    @Setter
    public static class Gemini {
        private boolean enabled = false;
        private String apiKey = "";
        private String model = "gemini-2.0-flash";
        private String endpoint = "https://generativelanguage.googleapis.com/v1beta";
    }

    @Getter
    @Setter
    public static class Openai {
        private boolean enabled = false;
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        private String endpoint = "https://api.openai.com/v1/chat/completions";
    }

    @Getter
    @Setter
    public static class Facebook {
        private boolean enabled = false;
        private String verifyToken = "";
    }
}
