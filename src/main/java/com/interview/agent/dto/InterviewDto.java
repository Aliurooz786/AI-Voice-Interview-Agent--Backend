package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for receiving data from the client to create a new interview.
 * This class captures the essential details needed to set up an interview session.
 */
@Data
public class InterviewDto {

    /**
     * The job title or position for which the interview is being created
     * (e.g., "Senior Java Developer").
     */
    private String jobPosition;

    /**
     * The detailed job description for the position. This will be used by the AI
     * to generate relevant questions.
     */
    private String jobDescription;
}