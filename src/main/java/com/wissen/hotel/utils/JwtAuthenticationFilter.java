package com.wissen.hotel.utils;

import com.wissen.hotel.models.User;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        logger.debug("Extracted token: {}", token);

        if (token != null && jwtUtil.validateToken(token)) {
            logger.debug("Token is valid");
            String userEmail = jwtUtil.extractEmail(token);
            logger.debug("Extracted userEmail: {}", userEmail);
            User user = userRepository.findByEmail(userEmail).orElse(null);
            logger.debug("User found: {}", user != null ? user.getEmail() : null);

            if (user != null) {
                // Convert role authorities to SimpleGrantedAuthority
                List<org.springframework.security.core.GrantedAuthority> authorities;
                authorities = user.getRole().getAuthorities().stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toList());
                logger.debug("Authorities: {}", authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authentication set in context");
            } else {
                logger.warn("User not found for email: {}", userEmail);
            }
        } else {
            logger.warn("Token is null or invalid");
        }

        filterChain.doFilter(request, response); // pass to next filter or controller
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        logger.debug("Raw Authorization header: {}", header);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Extracted token: {}", token);
            return token;
        }
        logger.warn("Authorization header missing or does not start with 'Bearer '");
        return null;
    }
}
