package com.uco.tutorspace_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutorSpaceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorSpaceApiApplication.class, args);
    }

}
