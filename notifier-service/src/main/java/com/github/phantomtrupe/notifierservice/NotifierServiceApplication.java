package com.github.phantomtrupe.notifierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient

public class NotifierServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifierServiceApplication.class, args);
    }

}
