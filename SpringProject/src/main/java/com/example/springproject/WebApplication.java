package com.example.springproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class WebApplication {
    private static final Logger log = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);

        String seedPassword = System.getenv("BCRYPT_SEED_PASSWORD");
        if (seedPassword == null || seedPassword.isBlank()) {
            log.info("Set BCRYPT_SEED_PASSWORD environment variable to emit a bcrypt hash after startup.");
            return;
        }

        String encodedPassword = new BCryptPasswordEncoder().encode(seedPassword);
        log.info("Generated bcrypt hash for supplied password.");
        log.debug("bcrypt hash: {}", encodedPassword);
    }
}
