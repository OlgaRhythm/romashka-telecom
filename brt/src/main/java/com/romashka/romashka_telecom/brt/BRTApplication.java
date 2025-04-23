package com.romashka.romashka_telecom.brt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class BRTApplication {

	public static void main(String[] args) {

		var ctx = SpringApplication.run(BRTApplication.class, args);
	}
}



