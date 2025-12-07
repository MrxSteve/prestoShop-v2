package com.devsteve.prestashopv2_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EntityScan("com.devsteve.prestashopv2_backend.models.entities")
@EnableJpaRepositories("com.devsteve.prestashopv2_backend.repositories")
public class Prestashopv2BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(Prestashopv2BackendApplication.class, args);
	}

}
