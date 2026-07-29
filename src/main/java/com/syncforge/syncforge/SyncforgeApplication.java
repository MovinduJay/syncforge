package com.syncforge.syncforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SyncforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SyncforgeApplication.class, args);
	}
}
