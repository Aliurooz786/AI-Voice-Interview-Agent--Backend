package com.interview.agent.security;

import com.interview.agent.service.UserService;
import com.interview.agent.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

/**
 * Handles successful OAuth2 logins.
 * Instead of creating a session, it finds/creates a user in the database,
 * generates a JWT token, and redirects the user back to the frontend
 * with the token in the URL.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private String frontendRedirectUrl = "http://localhost:5173/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        log.info("OAuth2 login successful for user: {}", email);

        userService.processOAuthUser(email, name);

        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(email);
        String jwtToken = jwtUtil.generateToken(userDetails);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", jwtToken)
                .build().toUriString();

        log.info("Redirecting user to frontend with JWT: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}