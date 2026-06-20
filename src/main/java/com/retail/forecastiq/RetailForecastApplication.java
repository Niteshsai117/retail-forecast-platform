package com.retail.forecastiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RetailForecastApplication {
    public static void main(String[] args) {
        SpringApplication.run(RetailForecastApplication.class, args);
    }
}
