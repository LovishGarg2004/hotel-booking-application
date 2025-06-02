package com.wissen.hotel.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public Cloudinary testCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "test-cloud");
        config.put("api_key", "test-key");
        config.put("api_secret", "test-secret");
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
