package com.wissen.hotel.controllers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @GetMapping("/test")
    public String testRoute() {
        return "Test route is working!";
    }
}