package com.foundit.foundit_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.foundit")
@EnableJpaRepositories("com.foundit.repository")
@EntityScan("com.foundit.model")
public class FoundItApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoundItApplication.class, args);
	}

}
