package org.example.bwxw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BwxwApplication {

    public static void main(String[] args) {
        SpringApplication.run(BwxwApplication.class, args);
    }

}
