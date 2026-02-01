package com.axacrate.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 *
 * TEMPORARY: This configuration DISABLES security for initial testing
 * Later, Member 1 will replace this with proper JWT authentication
 *
 * Current behavior: All endpoints are accessible without authentication
 */
@Configuration // Enables Spring Security
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security Filter Chain
     *
     * This configuration:
     * - Disables CSRF (Cross-Site Request Forgery) protection
     * - Allows ALL requests without authentication
     *
     * WARNING: Only for development/testing!
     * Member 1 will implement proper JWT security later
     *
     * @param http - HttpSecurity object to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF for testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Allow all requests (no authentication needed)
                );
        return http.build();
    }
}