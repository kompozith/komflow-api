package org.example.komflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "features")
public class KomflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KomflowApplication.class, args);
    }

}
