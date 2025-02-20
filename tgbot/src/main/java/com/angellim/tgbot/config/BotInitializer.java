package com.angellim.tgbot.config;

import com.angellim.tgbot.bot.MyAmazingBot;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//@Configuration
//public class BotInit {
//
//    private final MyAmazingBot myAmazingBot;
//
//    public BotInit(MyAmazingBot myAmazingBot) {
//        this.myAmazingBot = myAmazingBot;
//    }
//
//    @Bean
//    public TelegramLongPollingBot telegramBot() {
//        try {
//            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
//            telegramBotsApi.registerBot(myAmazingBot); // Регистрация бота
//            System.out.println("Telegram bot successfully registered.");
//        } catch (TelegramApiException e) {
//            System.err.println("Failed to register bot: " + e.getMessage());
//        }
//        return myAmazingBot;
//    }
//}


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInitializer {

    @Autowired
    MyAmazingBot bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException{
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(bot);
        }
        catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());

        }
    }

}
