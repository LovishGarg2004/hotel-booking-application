package com.wissen.hotel.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @GetMapping("/test")
    public String testRoute() {
        return "Dev 1234  nope !";
    }

    @GetMapping("/test2")
    public ResponseEntity<String> testRoute2() {
        return ResponseEntity.ok("Dev mope !");
    }
}
