package com.inxbtwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.inxbtwt.infra.properties.BucketUrlsProperties;

@SpringBootApplication
@EnableConfigurationProperties(BucketUrlsProperties.class)
public class InxbtwtApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InxbtwtApiApplication.class, args);
    }

}
