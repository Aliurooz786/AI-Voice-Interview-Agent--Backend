package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for sending a JWT token in the login API response.
 * This wraps the generated token in a structured JSON object (e.g., {"token": "..."}) for the client.
 */
@Data
public class LoginResponseDto {

    /**
     * The generated JSON Web Token (JWT) string for the authenticated user.
     */
    private String token;

    /**
     * Constructs a new LoginResponseDto with the provided JWT token.
     *
     * @param token The JWT token string to be included in the response.
     */
    public LoginResponseDto(String token) {
        this.token = token;
    }
}