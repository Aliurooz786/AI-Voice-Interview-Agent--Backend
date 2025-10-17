package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for sending user information in API responses.
 * This class provides a "safe" representation of a User, excluding sensitive
 * fields such as the password.
 */
@Data
public class UserResponseDto {

    /**
     * The unique identifier of the user.
     */
    private Long id;

    /**
     * The full name of the user.
     */
    private String fullName;

    /**
     * The email address of the user.
     */
    private String email;
}