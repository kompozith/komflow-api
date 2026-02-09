package com.kompozith.komflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KomflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KomflowApplication.class, args);
    }

}
