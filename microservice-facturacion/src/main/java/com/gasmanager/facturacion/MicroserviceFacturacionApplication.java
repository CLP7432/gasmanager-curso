package com.gasmanager.facturacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class MicroserviceFacturacionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceFacturacionApplication.class, args);
    }

}