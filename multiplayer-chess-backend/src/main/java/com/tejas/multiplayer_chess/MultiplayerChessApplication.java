package com.tejas.multiplayer_chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class MultiplayerChessApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiplayerChessApplication.class, args);
	}

}
