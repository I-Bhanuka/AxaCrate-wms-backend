package com.rfidwms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS Configuration
 *
 * CORS = Cross-Origin Resource Sharing
 * Allows frontend (React on localhost:3000) and ESP32 to call backend APIs
 *
 * Without this, browser blocks requests from different origins.  Because frontend is in another origin and backend is
 * in another. The CORS help with saying "it's OK to allow requests from these origins"
 */
@Configuration
public class CorsConfig {

    /**
     * CORS Filter Bean
     *
     * Configures which origins can access the backend:
     * - http://localhost:3000 (React frontend)
     * - http://localhost:8080 (Backend itself)
     * - Any ESP32 IP addresses
     *
     * @return CorsFilter with configuration
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow these origins (frontends) to access backend
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",     // React development server
                "http://localhost:8080",     // Backend
                "http://192.168.*.*"         // ESP32 devices on local network
        ));

        // Allow these HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Allow these headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        // Apply this configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}