package com.example.unitaskerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // BUNU EKLE

@SpringBootApplication
@EnableAsync // VE BUNU EKLE
public class UnitaskerBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnitaskerBackendApplication.class, args);
    }
}
