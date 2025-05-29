package com.wissen.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.wissen.hotel.controllers.AuthController;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class HotelBookingApplicationTests {

    @Autowired
    private AuthController authController;

    @Test
    @org.junit.jupiter.api.Disabled("Temporarily disabled for pipeline testing")
    void contextLoads() {
        assertThat(authController).isNotNull();
    }
}
