package com.zhijiao.foundation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FoundationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoundationApplication.class, args);
    }
}
