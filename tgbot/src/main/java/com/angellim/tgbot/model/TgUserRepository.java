package com.angellim.tgbot.model;

import org.springframework.data.repository.CrudRepository;

public interface TgUserRepository extends CrudRepository<TgUser, Long> {
    boolean existsByChatId(Long chatId);
}
