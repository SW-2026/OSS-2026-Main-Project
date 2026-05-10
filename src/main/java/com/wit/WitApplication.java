package com.wit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WitApplication {
    public static void main(String[] args) {
        SpringApplication.run(WitApplication.class, args);
    }
}
