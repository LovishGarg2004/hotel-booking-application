package com.wissen.hotel.utils;

import com.wissen.hotel.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Since you set the user object as the principal in JwtAuthenticationFilter
        if (principal instanceof User user) {
            return user;
        }

        return null;
    }
}
