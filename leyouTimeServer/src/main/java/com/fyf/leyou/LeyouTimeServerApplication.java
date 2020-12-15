package com.fyf.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class LeyouTimeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouTimeServerApplication.class, args);
    }

}
