package com.gasmanager.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MicroserviceVentasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceVentasApplication.class, args);
    }

}
