package com.wissen.hotel.services;

import com.wissen.hotel.utils.JwtAuthenticationFilter;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String username;

    @Value("${spring.security.user.password}")
    private String password;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public SecurityConfig(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userRepository);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**", "/test").permitAll()

                // Public GET hotel and room endpoints
                .requestMatchers(HttpMethod.GET,
                    "/api/hotels",
                    "/api/hotels/search",
                    "/api/hotels/top-rated",
                    "/api/hotels/{id}",
                    "/api/hotels/{id}/availability",
                    "/api/hotels/{id}/reviews",
                    "/api/hotels/{id}/rooms",
                    "/api/rooms/{id}",
                    "/api/rooms/{id}/availability",
                    "/api/rooms/types"
                ).permitAll()

                //User Endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/me/bookings").authenticated()

                // Room management
                .requestMatchers(HttpMethod.POST, "/api/rooms/hotel/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN")

                // Review endpoints
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                // Hotel endpoints
                .requestMatchers(HttpMethod.POST, "/api/hotels").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/hotels/{id}/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/hotels/owner").hasRole("HOTEL_OWNER")

                // Booking endpoints (NEW)
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/{id}").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/{id}").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/approve").hasAnyRole("HOTEL_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/{id}/cancel").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/hotels/{hotelId}").hasRole("HOTEL_OWNER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/{id}/invoice").hasAnyRole("CUSTOMER", "ADMIN")

                // Admin-only
                .requestMatchers("/api/users/admin/**").hasRole("ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()) // For H2 Console
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user);
    }
}
