package com.interview.agent.dto;

import com.interview.agent.enums.InterviewType; // Import the InterviewType enum
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for sending interview details in API responses.
 * This includes the interview specifics, generated topics, and safe user details.
 */
@Data
public class InterviewResponseDto {

    /**
     * The unique identifier of the interview.
     */
    private Long id;

    /**
     * The job position or title for the interview.
     */
    private String jobPosition;

    /**
     * The detailed job description for the position.
     */
    private String jobDescription;

    /**
     * The selected duration for the interview (e.g., "5 min").
     */
    private String duration;

    /**
     * The determined type of interview (MOCK or FULL).
     */
    private InterviewType interviewType;

    /**
     * The raw JSON string of AI-generated interview TOPICS.
     * (e.g., "[\"Java\", \"Spring Boot\", \"AWS\"]")
     * Can be null if topic generation failed.
     */
    private String topicsJson; // <-- Renamed from generatedQuestions

    /**
     * The timestamp indicating when the interview record was created.
     */
    private LocalDateTime createdAt;

    /**
     * A safe representation of the user who created the interview
     * (excludes sensitive data like password).
     */
    private UserResponseDto user;
}