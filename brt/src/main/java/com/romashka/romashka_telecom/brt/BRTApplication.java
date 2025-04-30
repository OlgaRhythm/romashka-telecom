package com.romashka.romashka_telecom.brt;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRabbit
public class BRTApplication {

	public static void main(String[] args) {
		SpringApplication.run(BRTApplication.class, args);
	}
}



