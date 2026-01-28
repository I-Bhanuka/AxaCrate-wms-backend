package com.rfidwms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bean Configuration
 *
 * Provides beans (objects) that Spring can inject into other classes
 * PasswordEncoder is used to hash passwords before storing in database
 */
@Configuration // Tells Spring: "This class configures beans"
public class BeanConfig {

    /**
     * Password Encoder Bean
     * Uses BCrypt algorithm to hash passwords
     *
     * Example:
     * String plainPassword = "admin123";
     * String hashed = passwordEncoder.encode(plainPassword);
     * // Result: "$2a$10$N9qo8uL..."
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean // Tells Spring: "Create this object and make it available everywhere"
    public PasswordEncoder passwordEncoder() {
        // This method is used to hash passwords. So we don't store plain text passwords
        return new BCryptPasswordEncoder(); // Kind of a hashing algorithm
    }
}