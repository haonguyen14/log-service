package com.haonguyen.logService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.haonguyen")
public class LogServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(LogServiceApplication.class, args);
	}
}
