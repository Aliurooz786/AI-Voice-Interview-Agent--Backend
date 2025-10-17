package com.interview.agent.controller;

import com.interview.agent.dto.LoginDto;
import com.interview.agent.dto.LoginResponseDto;
import com.interview.agent.dto.UserDto;
import com.interview.agent.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling user authentication endpoints.
 * This includes user registration and login. Endpoints under this controller are public.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles the user registration request.
     *
     * @param userDto DTO containing the new user's details (fullName, email, password).
     * @return A {@link ResponseEntity} with a success message and a 201 CREATED status,
     * or an error message with a 400 BAD REQUEST status if the email is already in use.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        log.info("Received registration request for email: {}", userDto.getEmail());
        try {
            userService.registerUser(userDto);
            log.info("Registration successful for email: {}. Sending 201 CREATED response.", userDto.getEmail());
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            log.error("Registration failed for email: {}. Reason: {}", userDto.getEmail(), e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles the user login request. Upon successful authentication, it returns a JWT token.
     *
     * @param loginDto DTO containing the user's login credentials (email, password).
     * @return A {@link ResponseEntity} containing a {@link LoginResponseDto} with the JWT token and a 200 OK status,
     * or an error message with a 401 UNAUTHORIZED status for invalid credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        log.info("Received login request for email: {}", loginDto.getEmail());
        try {
            String token = userService.loginUser(loginDto);
            log.info("Login successful for {}. Sending JWT.", loginDto.getEmail());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (AuthenticationException e) {
            log.error("Login failed for {}: Invalid credentials.", loginDto.getEmail());
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
}