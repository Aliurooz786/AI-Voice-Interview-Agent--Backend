package com.interview.agent.controller;

import com.interview.agent.dto.LoginDto;
import com.interview.agent.dto.LoginResponseDto;
import com.interview.agent.dto.UserDto;
import com.interview.agent.dto.UserResponseDto; // Ensure this is imported
import com.interview.agent.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Ensure this is imported
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder; // Ensure this is imported
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Ensure this is imported
import org.springframework.web.bind.annotation.GetMapping; // Ensure this is imported
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling user authentication and profile endpoints.
 * Public endpoints: /register, /login.
 * Secure endpoints: /me.
 */
@RestController
@RequestMapping("/api/auth") // Base path for all endpoints in this controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles the user registration request. Public endpoint.
     *
     * @param userDto DTO containing new user details.
     * @return ResponseEntity with success message (201) or error message (400).
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        log.info("Received registration request for email: {}", userDto.getEmail());
        try {
            userService.registerUser(userDto);
            log.info("Registration successful for email: {}. Sending 201 CREATED.", userDto.getEmail());
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // This exception is thrown by UserService if email is already in use
            log.error("Registration failed for email {}: {}", userDto.getEmail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Catch any other unexpected errors during registration
            log.error("Unexpected error during registration for email {}: {}", userDto.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>("An unexpected error occurred during registration.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles the user login request. Returns a JWT token upon successful authentication.
     * Public endpoint.
     *
     * @param loginDto DTO containing login credentials.
     * @return ResponseEntity with JWT token (200) or error message (401).
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        log.info("Received login request for email: {}", loginDto.getEmail());
        try {
            String token = userService.loginUser(loginDto);
            log.info("Login successful for {}. Sending JWT.", loginDto.getEmail());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (AuthenticationException e) {
            // This exception is thrown by AuthenticationManager if credentials are invalid
            log.error("Login failed for {}: Invalid credentials.", loginDto.getEmail());
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // Catch any other unexpected errors during login
            log.error("Unexpected error during login for email {}: {}", loginDto.getEmail(), e.getMessage(), e);
            return new ResponseEntity<>("An unexpected error occurred during login.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the profile details (excluding password) of the currently authenticated user.
     * Secure endpoint (requires valid JWT). Accessible via GET /api/auth/me.
     *
     * @return ResponseEntity containing the {@link UserResponseDto} (200),
     * or an error status (401, 404, 500).
     */
    @GetMapping("/me") // Endpoint for fetching current user profile
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verify that the user is properly authenticated
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempt to access /me endpoint without valid authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        String userEmail = authentication.getName(); // Get username (email) from security context
        log.info("Request received to get profile for authenticated user: {}", userEmail);

        try {
            // Fetch user details using the service method
            UserResponseDto userProfile = userService.getUserDetailsByEmail(userEmail);
            return ResponseEntity.ok(userProfile); // Return 200 OK with user profile DTO
        } catch (UsernameNotFoundException e) {
            // This case indicates an inconsistency (authenticated user not in DB)
            log.error("CRITICAL: Authenticated user '{}' not found in database during profile fetch.", userEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User profile data not found.");
        } catch (Exception e) {
            // Catch any other unexpected errors during service call
            log.error("Unexpected error fetching profile for user {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching user profile.");
        }
    }
}