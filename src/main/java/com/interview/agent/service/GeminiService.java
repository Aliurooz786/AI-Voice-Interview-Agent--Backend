package com.interview.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * A client service to interact with the Google Gemini API using RestTemplate.
 * This service loads the TOPIC generation prompt and makes the HTTP call.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourceLoader resourceLoader;

    private String topicsPromptTemplate;

    public GeminiService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Loads the topics prompt template on application startup.
     */
    @PostConstruct
    public void loadPrompts() {
        try {
            topicsPromptTemplate = loadPromptFromFile("prompts/gemini_topics_prompt.txt");
            log.info("Successfully loaded topics prompt template.");
        } catch (IOException e) {
            log.error("Failed to load critical topics prompt file 'gemini_topics_prompt.txt'.", e);
            throw new RuntimeException("Failed to load critical topics prompt file.", e);
        }
    }

    /**
     * Generates a list of interview TOPICS (not questions) based on job details.
     *
     * @param jobPosition    The job title.
     * @param jobDescription The job description.
     * @return A clean JSON string array of topics (e.g., "[\"Java\", \"AWS\"]").
     * @throws RuntimeException if the API call fails.
     */
    public String generateTopics(String jobPosition, String jobDescription) {
        log.info("Generating topics for: {}", jobPosition);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // Build the prompt by replacing placeholders
        String prompt = topicsPromptTemplate
                .replace("{jobPosition}", jobPosition)
                .replace("{jobDescription}", jobDescription);

        try {
            // Call the reusable helper method to execute the API call
            String rawResponse = callGeminiApi(prompt, endpoint);
            // Clean the response (removes ```json etc.)
            return cleanGeminiResponse(rawResponse, jobPosition);
        } catch (Exception e) {
            log.error("Failed to call Gemini API for topics: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling Gemini API for topics: " + e.getMessage(), e);
        }
    }

    // --- Private Helper Methods ---

    /**
     * Helper method to read a prompt file from the classpath.
     */
    private String loadPromptFromFile(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + filePath);
        }
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    /**
     * Reusable private method to make the actual API call to Gemini.
     */
    private String callGeminiApi(String prompt, String endpoint) throws Exception {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", Collections.singletonList(part));
        Map<String, Object> body = Map.of("contents", Collections.singletonList(content));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("Calling Gemini endpoint (length: {})", prompt.length());
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                endpoint, HttpMethod.POST, entity, String.class
        );
        return responseEntity.getBody();
    }

    /**
     * Reusable private method to parse and clean the JSON response from Gemini.
     */
    private String cleanGeminiResponse(String rawResponse, String context) throws IOException {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode()) {
            log.error("Missing 'text' field in Gemini response for {}. Full response: {}", context, rawResponse);
            throw new IOException("Gemini response did not contain the expected 'text' field.");
        }

        String aiText = textNode.asText();
        // Cleaning logic (removes markdown)
        String cleanedAiText = aiText.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();
        log.info("Cleaned AI text extracted successfully for {}.", context);
        return cleanedAiText;
    }
}