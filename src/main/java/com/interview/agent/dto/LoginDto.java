package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for receiving user login credentials from the client.
 * This class captures the email and password provided during an authentication attempt.
 */
@Data
public class LoginDto {

    /**
     * The user's email address, used for identification during authentication.
     */
    private String email;

    /**
     * The user's plain-text password, provided for authentication.
     */
    private String password;
}