package br.com.ufape.spendfy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeApiService {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String chat(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Anthropic API key não configurada. Retornando resposta padrão.");
            return "API key não configurada";
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("model", MODEL);
                put("max_tokens", 512);
                put("messages", java.util.List.of(
                        java.util.Map.of("role", "user", "content", prompt)
                ));
            }});

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANTHROPIC_API_URL))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Erro na Claude API: status={}, body={}", response.statusCode(), response.body());
                return "Erro ao consultar IA";
            }

            JsonNode json = objectMapper.readTree(response.body());
            return json.at("/content/0/text").asText("Resposta indisponível");

        } catch (Exception e) {
            log.error("Falha ao chamar Claude API", e);
            return "Erro ao processar resposta da IA";
        }
    }
}
