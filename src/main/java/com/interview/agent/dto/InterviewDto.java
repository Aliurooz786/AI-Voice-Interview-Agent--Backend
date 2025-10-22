package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for receiving data from the client to create a new interview.
 * This class captures the essential details needed to set up an interview session,
 * including job specifics and desired duration.
 */
@Data
public class InterviewDto {

    /**
     * The job title or position for the interview (e.g., "Senior Java Developer").
     */
    private String jobPosition;

    /**
     * The detailed job description, used by the AI to generate relevant questions.
     */
    private String jobDescription;

    /**
     * The desired duration for the interview, selected by the user
     * (e.g., "5 min", "15 min", "30 min").
     */
    private String duration;
}