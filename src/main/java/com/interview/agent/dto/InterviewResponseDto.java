package com.interview.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for sending interview details in API responses.
 * This class provides a "safe" representation of an Interview, including details
 * about the user who created it, without exposing sensitive information.
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
     * The timestamp indicating when the interview was created.
     */
    private LocalDateTime createdAt;

    /**
     * A safe representation of the user who created the interview.
     * Uses {@link UserResponseDto} to avoid exposing the user's password.
     */
    private UserResponseDto user;
}