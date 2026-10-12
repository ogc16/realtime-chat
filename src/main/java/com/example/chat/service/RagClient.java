package com.example.chat.service;

import com.example.chat.config.RagProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

/**
 * Thin HTTP client for the embedchain RAG sidecar. Returns an empty result when
 * the integration is disabled or the sidecar is unreachable, so the core chat
 * keeps working without it.
 */
@Service
public class RagClient {

    private final RagProperties properties;
    private final RestClient restClient;

    public RagClient(RagProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    public Optional<String> ask(String question) {
        if (!properties.isEnabled() || question == null || question.isBlank()) {
            return Optional.empty();
        }
        try {
            RagAnswer answer = restClient.post()
                    .uri("/query")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("question", question))
                    .retrieve()
                    .body(RagAnswer.class);
            return Optional.ofNullable(answer).map(RagAnswer::answer);
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    public record RagAnswer(String answer) {
    }
}
