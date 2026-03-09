package com.reciprocityledger.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.reciprocityledger.backend")
@SpringBootApplication
public class ReciprocityLedgerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReciprocityLedgerBackendApplication.class, args);
    }
}
