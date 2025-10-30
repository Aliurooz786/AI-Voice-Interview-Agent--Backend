package com.interview.agent.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for receiving user registration data from the client.
 * This class captures the necessary information to create a new user account.
 */
@Data
public class UserDto {

    /**
     * The desired full name for the new user.
     */
    private String fullName;

    /**
     * The desired email address for the new user. This will be used for  login.
     */
    private String email;

    /**
     * The user's desired plain-text password.
     * This will be hashed by the service before being stored in the database.
     */
    private String password;
}