package com.example.sportbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportBookingApplication.class, args);
    }
}
