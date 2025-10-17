package com.interview.agent.config;

import com.interview.agent.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main configuration class for Spring Security.
 * This class sets up the security filter chain, password encoding,
 * and the authentication manager.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the password encoder bean.
     * We use BCrypt, which is a strong, industry-standard hashing algorithm for passwords.
     *
     * @return The PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * This is required by the UserService to process authentication requests.
     *
     * @param config The authentication configuration.
     * @return The AuthenticationManager instance.
     * @throws Exception if an error occurs while getting the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the main security filter chain for the application.
     * This is the central point for defining all security rules.
     *
     * @param http          The HttpSecurity object to configure.
     * @param jwtAuthFilter The custom JWT filter to be added to the chain.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // 1. Disable CSRF (Cross-Site Request Forgery) protection, as we are using stateless JWT authentication.
                .csrf(csrf -> csrf.disable())

                // 2. Define authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Publicly allow all endpoints under /api/auth/ (e.g., register, login).
                        .anyRequest().authenticated() // All other requests must be authenticated.
                )

                // 3. Configure session management to be stateless. The server will not create or maintain any sessions.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Add our custom JWT authentication filter before the standard username/password authentication filter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}