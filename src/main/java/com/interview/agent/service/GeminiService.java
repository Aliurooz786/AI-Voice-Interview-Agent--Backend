package com.interview.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * A client service to interact with the Google Gemini API using RestTemplate.
 * Builds prompts, makes HTTP requests, parses the response, and cleans the output.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates interview questions via Gemini API based on job details and duration.
     *
     * @param jobPosition    The job title.
     * @param jobDescription The job description.
     * @param duration       The interview duration (e.g., "5 min").
     * @return A clean JSON string representing an array of questions, or throws RuntimeException on failure.
     */
    public String generateQuestions(String jobPosition, String jobDescription, String duration) {
        log.info("Generating questions for: {} (Duration: {}) using RestTemplate", jobPosition, duration);

        // --- USING YOUR CONFIRMED WORKING ENDPOINT ---
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        int numberOfQuestions;
        String interviewFocus;
        if ("5 min".equalsIgnoreCase(duration)) {
            numberOfQuestions = 3;
            interviewFocus = "Focus on 1-2 core technical skills. Keep questions concise for screening.";
        } else if ("15 min".equalsIgnoreCase(duration)) {
            numberOfQuestions = 5;
            interviewFocus = "Provide a balanced mix of technical and behavioral questions.";
        } else {
            numberOfQuestions = 7;
            interviewFocus = "Generate in-depth technical and behavioral questions.";
        }

        String prompt = String.format(
                "You are an expert interviewer creating questions for a '%s' role described as: '%s'. " +
                        "Generate exactly %d insightful questions for a '%s' interview. " +
                        "%s " +
                        "Response MUST be ONLY a valid, minified JSON array of objects. Each object needs 'question', 'category', 'type' fields. " +
                        "Do not include ```json markdown. " +
                        "Example: [{\"question\":\"...\",\"category\":\"...\",\"type\":\"...\"}]",
                jobPosition, jobDescription, numberOfQuestions, duration, interviewFocus
        );

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", Collections.singletonList(part));
        Map<String, Object> body = Map.of("contents", Collections.singletonList(content));

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.debug("Calling Gemini endpoint: {}", endpoint);
            ResponseEntity<String> responseEntity = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            String response = responseEntity.getBody();
            log.debug("Gemini raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (textNode.isMissingNode()) {
                log.error("Missing 'text' field in Gemini response. Full response: {}", response);
                throw new RuntimeException("Gemini response did not contain the expected 'text' field.");
            }

            String aiText = textNode.asText();

            // Clean the response: Remove potential markdown code blocks
            String cleanedAiText = aiText.trim()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            log.info("Cleaned AI text extracted successfully for {} ({})", jobPosition, duration);
            return cleanedAiText; // Return the cleaned JSON string

        } catch (Exception e) {
            log.error("Failed to call Gemini API or parse response for {} ({}). Error: {}", jobPosition, duration, e.getMessage(), e);
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }
}