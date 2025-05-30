package com.wissen.hotel.services;

import com.wissen.hotel.utils.JwtAuthenticationFilter;
import com.wissen.hotel.repositories.UserRepository;
import com.wissen.hotel.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.context.annotation.Configuration;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080")); // Specific frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Disable CSRF only for API endpoints
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints
                    auth.requestMatchers(
                            "/api/auth/**",
                            "/api/pricing/calculate",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/h2-console/**").permitAll();

                    auth.requestMatchers("/test").permitAll();
                    auth.requestMatchers("/api/images/**").permitAll();
                    auth.requestMatchers("/api/hotels/*/images").permitAll();
                    auth.requestMatchers("/api/rooms/*/images").permitAll();

                    // Public GET hotel and room endpoints
                    auth.requestMatchers(HttpMethod.GET,
                            "/api/hotels",
                            "/api/hotels/search",
                            "/api/hotels/top-rated",
                            "/api/hotels/{id}",
                            "/api/hotels/{id}/availability",
                            "/api/hotels/{id}/reviews",
                            "/api/hotels/{id}/rooms",
                            "/api/rooms/{id}",
                            "/api/rooms/{id}/availability",
                            "/api/rooms/types").permitAll();

                    // User Endpoints
                    auth.requestMatchers(HttpMethod.GET, "/api/users/me/bookings").authenticated();

                    // Room management
                    auth.requestMatchers(HttpMethod.POST, "/api/rooms/hotel/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN");

                    // Review endpoints
                    auth.requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/reviews").authenticated();
                    auth.requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated();

                    // Hotel endpoints
                    auth.requestMatchers(HttpMethod.POST, "/api/hotels").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/hotels/*/approve").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/hotels/owner").hasRole("HOTEL_OWNER");

                    // Room management (protected)
                    auth.requestMatchers(HttpMethod.POST, "/api/rooms/hotel/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("HOTEL_OWNER", "ADMIN");

                    // Admin-only
                    auth.requestMatchers("/api/users/admin/**").hasRole("ADMIN");

                    // ======== Pricing Rules Authorization ========
                    // Create/Update/Delete pricing rules
                    auth.requestMatchers(HttpMethod.POST, "/api/pricing/rules").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/pricing/rules/**").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/pricing/rules/**").hasAnyRole("HOTEL_OWNER", "ADMIN");

                    // Get pricing rules
                    auth.requestMatchers(HttpMethod.GET, "/api/pricing/rules").hasAnyRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/pricing/rules/**").hasAnyRole("HOTEL_OWNER", "ADMIN");

                    // Hotel-specific pricing rules
                    auth.requestMatchers(HttpMethod.GET, "/api/pricing/rules/hotels/**").hasAnyRole("HOTEL_OWNER",
                            "ADMIN");

                    // Price simulation
                    auth.requestMatchers(HttpMethod.POST, "/api/pricing/simulate").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    // ======== End Pricing Rules ========

                    // Amenity endpoints
                    auth.requestMatchers(HttpMethod.GET, "/api/amenities", "/api/amenities/{id}").permitAll(); // Public
                                                                                                               // access
                                                                                                               // for
                                                                                                               // listing
                                                                                                               // and
                                                                                                               // viewing
                                                                                                               // amenities
                    auth.requestMatchers(HttpMethod.POST, "/api/amenities").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/amenities/{id}").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/amenities/{id}").hasRole("ADMIN");

                    // Room Amenity endpoints
                    auth.requestMatchers(HttpMethod.GET, "/api/rooms/*/amenities").permitAll(); // Public access to view
                                                                                                // room amenities
                    auth.requestMatchers(HttpMethod.POST, "/api/rooms/*/amenities").hasAnyRole("HOTEL_OWNER", "ADMIN"); // Only
                                                                                                                        // ADMIN
                                                                                                                        // and
                                                                                                                        // HOTEL_OWNER
                                                                                                                        // can
                                                                                                                        // add
                    auth.requestMatchers(HttpMethod.DELETE, "/api/rooms/*/amenities/*").hasAnyRole("HOTEL_OWNER",
                            "ADMIN"); // Only ADMIN and HOTEL_OWNER can remove

                    // Image endpoints
                    auth.requestMatchers(HttpMethod.POST, "/api/images/upload").authenticated();
                    auth.requestMatchers(HttpMethod.POST, "/api/hotels/*/images").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/rooms/*/images").hasAnyRole("HOTEL_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/images/*").hasAnyRole("HOTEL_OWNER", "ADMIN");

                    // Any other request must be authenticated
                    auth.anyRequest().authenticated();
                })
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
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
