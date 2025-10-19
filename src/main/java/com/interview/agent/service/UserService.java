package com.interview.agent.service;

import com.interview.agent.dto.LoginDto;
import com.interview.agent.dto.UserDto;
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

/**
 * Service class for handling user-related business logic, including registration,
 * authentication, and loading user details for Spring Security.
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
     * Registers a new user in the system.
     * It checks if the email is already in use, hashes the password, and saves the new user to the database.
     *
     * @param userDto DTO containing new user details (fullName, email, password).
     * @return The saved User entity.
     * @throws IllegalStateException if the email is already registered.
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
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * @param loginDto DTO containing user's email and password.
     * @return A JWT token string for the authenticated user.
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
     * Loads a user's details by their email address.
     * This method is required by Spring Security's UserDetailsService interface.
     *
     * @param email The email of the user to load.
     * @return A UserDetails object containing user information.
     * @throws UsernameNotFoundException if no user is found with the given email.
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
}