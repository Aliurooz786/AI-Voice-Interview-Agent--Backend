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
 * This service is responsible for building prompts, making HTTP requests,
 * and parsing the AI-generated response.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates interview questions for a given job position and description by calling the Gemini API.
     *
     * @param jobPosition    The title of the job (e.g., "Senior Java Developer").
     * @param jobDescription The detailed description of the job role and requirements.
     * @return A raw JSON string representing an array of generated questions, or an error JSON on failure.
     */
    public String generateQuestions(String jobPosition, String jobDescription) {
        log.info("Attempting to generate questions using proven RestTemplate config for: {}", jobPosition);


        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String prompt = String.format(
                "You are a world-class hiring manager and senior technical architect at a top tech company. " +
                        "Your goal is to create insightful, non-trivial interview questions that accurately assess a candidate's real-world problem-solving abilities, not just their memorization of facts. " +
                        "Based on the job position of '%s' and the following detailed job description: '%s', generate exactly 5 interview questions. " +
                        "Your response MUST be ONLY a valid, minified JSON array of objects. " +
                        "Do not include any text, explanations, or markdown formatting like ```json before or after the array. " +
                        "Each object in the array must have exactly three string fields: 'question', 'category', and 'type'. " +
                        "The 'question' field should contain the full, thought-provoking question text. Avoid simple 'What is X?' questions. " +
                        "The 'category' field should be a specific technical skill or behavioral trait derived from the job description (e.g., 'Spring Security', 'React State Management', 'Team Collaboration'). " +
                        "The 'type' field must be one of two exact values: 'Technical' or 'Behavioral'. " +
                        "Example of the required output format: [{\"question\":\"...\",\"category\":\"...\",\"type\":\"...\"}]",
                jobPosition, jobDescription
        );

        // Manually construct the JSON request body as per the Google API specification.
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", Collections.singletonList(part));
        Map<String, Object> body = Map.of("contents", Collections.singletonList(content));

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String response = responseEntity.getBody();
            log.debug("Gemini raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            if (textNode.isMissingNode()) {
                log.error("Missing 'text' field in Gemini response. Full response: {}", response);
                return "{\"error\": \"Missing 'text' in Gemini response.\"}";
            }

            String aiText = textNode.asText();
            log.info("Extracted AI text successfully for job position: {}", jobPosition);
            return aiText;

        } catch (Exception e) {
            log.error("Failed to call Gemini API or parse response. Error: {}", e.getMessage(), e);
            return "{\"error\": \"Error calling Gemini API: " + e.getMessage() + "\"}";
        }
    }
}