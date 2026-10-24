package com.inxbtwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.inxbtwt.infra.properties.BucketUrlsProperties;
import com.inxbtwt.infra.properties.SecurityRoutesProperties;
import com.inxbtwt.infra.properties.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({BucketUrlsProperties.class, SecurityRoutesProperties.class, JwtProperties.class})
public class InxbtwtApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InxbtwtApiApplication.class, args);
    }

}
