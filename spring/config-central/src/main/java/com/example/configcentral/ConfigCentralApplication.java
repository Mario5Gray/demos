package com.example.configcentral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigCentralApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigCentralApplication.class, args);
	}
}
