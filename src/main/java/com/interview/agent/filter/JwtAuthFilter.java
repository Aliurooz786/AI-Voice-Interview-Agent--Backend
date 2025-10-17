package com.interview.agent.filter;

import com.interview.agent.service.UserService;
import com.interview.agent.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A Spring Security filter that intercepts incoming requests to validate JWT tokens.
 * This filter is executed once per request. It extracts the token from the
 * 'Authorization' header, validates it, and sets the user's authentication
 * in the SecurityContextHolder if the token is valid.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * The core filtering logic. This method is called for each HTTP request.
     *
     * @param request     The incoming HttpServletRequest.
     * @param response    The outgoing HttpServletResponse.
     * @param filterChain The filter chain to pass the request along.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.debug("Intercepting request for URL: {}", request.getRequestURI());

        // 1. Check if the Authorization header is present and correctly formatted.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization header is missing or does not start with 'Bearer '. Passing to next filter.");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the JWT from the header.
        jwt = authHeader.substring(7);

        try {
            userEmail = jwtUtil.extractUsername(jwt);
            log.debug("Extracted username '{}' from JWT.", userEmail);

            // 3. Check if the user is not already authenticated in the current security context.
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userService.loadUserByUsername(userEmail);

                // 4. Validate the token against the user details.
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    log.info("JWT is valid for user '{}'. Setting authentication in SecurityContext.", userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are not needed as we are using JWT
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Set the authentication in the security context.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("JWT validation failed for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT. Error: {}", e.getMessage());
        }

        // 6. Pass the request along to the next filter in the chain.
        filterChain.doFilter(request, response);
    }
}