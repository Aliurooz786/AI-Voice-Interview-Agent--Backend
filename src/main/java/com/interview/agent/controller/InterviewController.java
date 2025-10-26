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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * REST controller for handling interview-related API endpoints.
 * Requires user authentication for all endpoints.
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
     * Creates a new interview record AND generates AI-powered TOPICS in one single step.
     * Accessible only by authenticated users.
     *
     * @param interviewDto DTO containing job position, description, and duration.
     * @return ResponseEntity with created interview details (including topics) and HTTP status 201 (Created),
     * or an error status (401, 500) if creation or topic generation fails.
     */
    @PostMapping
    public ResponseEntity<?> createInterviewAndGenerateTopics(@RequestBody InterviewDto interviewDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        log.info("Request received to create interview and generate TOPICS for user: {}", userEmail);
        try {
            // Call the updated service method
            InterviewResponseDto createdInterviewDto = interviewService.createInterviewAndGenerateTopics(interviewDto, userEmail);
            return new ResponseEntity<>(createdInterviewDto, HttpStatus.CREATED);
        } catch (UsernameNotFoundException e) {
            log.error("User not found during interview creation: {}", userEmail, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User authentication error.");
        } catch (RuntimeException e) { // Catches exceptions from service (e.g., Gemini failure)
            log.error("Failed to create interview or generate TOPICS for user {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process interview creation: " + e.getMessage());
        }
    }

    /**
     * Retrieves the details of a single interview by its ID, including generated topics.
     *
     * @param interviewId The ID of the interview to retrieve.
     * @return ResponseEntity containing the interview details (DTO) or a 404 error if not found.
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDto> getInterviewById(@PathVariable Long interviewId) {
        log.info("Request received to get details for interview ID: {}", interviewId);
        try {
            InterviewResponseDto interviewDto = interviewService.getInterviewById(interviewId);
            return ResponseEntity.ok(interviewDto);
        } catch (RuntimeException e) { // Catches InterviewNotFoundException from service
            log.error("Error retrieving interview details for ID {}: {}", interviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 without body for not found
        }
    }

    /**
     * Retrieves all interviews created by the currently authenticated user.
     * Used for populating the user's dashboard.
     *
     * @return ResponseEntity containing a list of the user's interviews (DTOs) or an error status.
     */
    @GetMapping
    public ResponseEntity<List<InterviewResponseDto>> getUserInterviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        log.info("Request received to get all interviews for user: {}", userEmail);

        try {
            List<InterviewResponseDto> interviews = interviewService.getInterviewsByUser(userEmail);
            return ResponseEntity.ok(interviews);
        } catch (UsernameNotFoundException e) {
            log.error("Authenticated user not found while fetching interviews: {}", userEmail);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching interviews for user {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}