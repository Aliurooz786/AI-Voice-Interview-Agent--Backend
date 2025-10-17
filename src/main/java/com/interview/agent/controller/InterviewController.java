package com.interview.agent.controller;

import com.interview.agent.dto.InterviewDto;
import com.interview.agent.dto.InterviewResponseDto;
import com.interview.agent.service.InterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling all API endpoints related to interviews.
 * All endpoints under this controller require user authentication.
 */
@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private static final Logger log = LoggerFactory.getLogger(InterviewController.class);

    private final InterviewService interviewService;

    @Autowired
    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * Creates a new interview based on the provided job details.
     * This endpoint is secure and can only be accessed by an authenticated user.
     *
     * @param interviewDto DTO containing the job position and description.
     * @return A {@link ResponseEntity} with the created interview details and a 201 CREATED status.
     */
    @PostMapping
    public ResponseEntity<InterviewResponseDto> createInterview(@RequestBody InterviewDto interviewDto) {
        // Get the email of the currently authenticated user from the security context.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        log.info("Received request to create a new interview for user: {}", userEmail);

        InterviewResponseDto createdInterviewDto = interviewService.createInterview(interviewDto, userEmail);

        return new ResponseEntity<>(createdInterviewDto, HttpStatus.CREATED);
    }

    /**
     * Triggers the generation of AI-powered questions for a specific interview.
     * The generated questions are saved to the interview record in the database.
     *
     * @param interviewId The ID of the interview to generate questions for.
     * @return A {@link ResponseEntity} containing the raw JSON string of generated questions.
     */
    @PostMapping("/{interviewId}/generate-questions")
    public ResponseEntity<String> generateQuestions(@PathVariable Long interviewId) {
        log.info("Received request to generate AI questions for interview ID: {}", interviewId);
        try {
            String questionsJson = interviewService.generateAndSaveQuestions(interviewId);
            return ResponseEntity.ok(questionsJson);
        } catch (RuntimeException e) {
            log.error("Could not generate questions for interview ID {}: {}", interviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}