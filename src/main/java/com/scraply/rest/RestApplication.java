package com.scraply.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class RestApplication {

	private final Environment environment;

	public RestApplication(Environment environment) {
		this.environment = environment;
	}

	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}

//	@EventListener(ApplicationReadyEvent.class)
//	public void onApplicationReady() {
//		String port = environment.getProperty("server.port", "8080");
//		log.info("=================================================");
//		log.info("  Scraply Application started successfully");
//		log.info("  Connected to PostgreSQL database");
//		log.info("  Server running on port: {}", port);
//		log.info("  Swagger UI: http://localhost:{}/swagger-ui.html", port);
//		log.info("=================================================");
//	}
}

