package com.interview.agent.service;

import com.interview.agent.dto.LoginDto;
import com.interview.agent.dto.UserDto;
import com.interview.agent.dto.UserResponseDto;
import com.interview.agent.model.User;
import com.interview.agent.repository.UserRepository;
import com.interview.agent.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Service class for handling user-related business logic, including registration,
 * authentication, and loading user details for Spring Security.
 * (Updated to support OAuth2 user processing)
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user in the system (Password-based).
     * (No changes here)
     */
    public User registerUser(UserDto userDto) {
        log.info("Attempting to register user with email: {}", userDto.getEmail());
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already in use.", userDto.getEmail());
            throw new IllegalStateException("Email already in use.");
        }
        User newUser = new User();
        newUser.setFullName(userDto.getFullName());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        User savedUser = userRepository.save(newUser);
        log.info("User with email {} successfully saved to database with ID: {}", savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }

    /**
     * Authenticates a password-based user and generates a JWT token.
     * (No changes here)
     */
    public String loginUser(LoginDto loginDto) {
        log.info("Attempting to authenticate user: {}", loginDto.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );
        log.info("User {} authenticated successfully.", loginDto.getEmail());

        final UserDetails userDetails = loadUserByUsername(loginDto.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        log.info("JWT generated for user: {}", loginDto.getEmail());
        return jwt;
    }

    /**
     * Finds or creates a user based on Google OAuth2 login.
     * If the user does not exist, a new account is created.
     *
     * @param email The email received from Google.
     * @param name  The full name received from Google.
     */
    public void processOAuthUser(String email, String name) {
        log.info("Processing OAuth2 user: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {

            log.info("OAuth2 user already exists in database: {}", email);
            User user = userOptional.get();
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
                userRepository.save(user);
                log.info("Updated user's full name from Google: {}", email);
            }
        } else {
            log.info("OAuth2 user not found. Creating new user: {}", email);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(passwordEncoder.encode("OAUTH2_USER_NO_PASSWORD"));

            userRepository.save(newUser);
            log.info("New OAuth2 user created successfully: {}", email);
        }
    }


    /**
     * Loads a user's details by their email address (for Spring Security).
     * (No changes here)
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }


    /**
     * Retrieves the non-sensitive details of a user by their email.
     * (No changes here)
     */
    public UserResponseDto getUserDetailsByEmail(String email) {
        log.debug("Fetching user details for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User details requested for non-existent email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(user.getId());
        userDto.setFullName(user.getFullName());
        userDto.setEmail(user.getEmail());

        log.info("Successfully fetched user details for email: {}", email);
        return userDto;
    }
}