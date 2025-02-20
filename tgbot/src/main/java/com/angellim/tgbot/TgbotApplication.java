package com.angellim.tgbot;

import com.angellim.tgbot.config.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
public class TgbotApplication {
	public static void main(String[] args) {
		SpringApplication.run(TgbotApplication.class, args);
		System.out.println("Spring Context started!");
	}
}
