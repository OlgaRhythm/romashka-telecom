package com.romashka.romashka_telecom;

import com.romashka.romashka_telecom.service.CallsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RomashkaTelecomApplication {

	public static void main(String[] args) {
		SpringApplication.run(RomashkaTelecomApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(CallsService callsService) {
		return args -> {
			callsService.generateCalls();
//			callsService.shutdown();
		};
	}
}
