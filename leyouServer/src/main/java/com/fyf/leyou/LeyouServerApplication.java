package com.fyf.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class LeyouServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeyouServerApplication.class, args);
	}

}
