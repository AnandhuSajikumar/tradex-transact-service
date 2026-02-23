package com.spring.tradextransactservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TradexTransactServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradexTransactServiceApplication.class, args);
    }

}
