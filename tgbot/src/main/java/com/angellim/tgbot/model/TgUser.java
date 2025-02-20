package com.angellim.tgbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
@Entity(name="tguser_table")
public class TgUser {

    @Id
    private Long chatId;

    private String firstName;
    private String lastName;
    private String username;
    private Timestamp registeredAt;


    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
