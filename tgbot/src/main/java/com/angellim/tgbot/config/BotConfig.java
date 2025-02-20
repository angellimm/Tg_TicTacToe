package com.angellim.tgbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {

    private String token;
    private String botName;
    private Long botOwnerId;

}