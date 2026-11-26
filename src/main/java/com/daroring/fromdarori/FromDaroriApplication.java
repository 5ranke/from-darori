package com.daroring.fromdarori;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FromDaroriApplication {

    public static void main(String[] args) {
        SpringApplication.run(FromDaroriApplication.class, args);
    }

}
