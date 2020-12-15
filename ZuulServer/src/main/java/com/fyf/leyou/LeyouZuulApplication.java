package com.fyf.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class LeyouZuulApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouZuulApplication.class, args);
    }

}
