package com.wissen.hotel.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Enable Java 8 Date/Time module
        mapper.registerModule(new JavaTimeModule());

        // Disable timestamps for dates
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Enable case-insensitive enums
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

        // Optional: handle unknown properties gracefully
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }
}
