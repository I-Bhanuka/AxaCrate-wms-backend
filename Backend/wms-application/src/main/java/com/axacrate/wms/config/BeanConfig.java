package com.axacrate.wms.config;

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

    
}