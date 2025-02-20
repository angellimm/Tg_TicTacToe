package com.angellim.tgbot.bot;

import com.angellim.tgbot.config.BotConfig;
import com.angellim.tgbot.model.TgUser;
import com.angellim.tgbot.model.TgUserRepository;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;


@Slf4j
@Component
public class MyAmazingBot extends TelegramLongPollingBot {

    @Autowired
    private TgUserRepository userRepository;

    final BotConfig botConfig;

    private Game game;

    private Map<String, GameRoom> gameRooms = new HashMap<>();

    private Map<Long, String> userToRoomMap = new HashMap<>();


    public MyAmazingBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        log.info("BotConfig loaded: token={}, botName={}, botNameOwnerId={}",
               botConfig.getToken(), botConfig.getBotName(), botConfig.getBotOwnerId());

        List<BotCommand> listofCommons = new ArrayList<>();
        listofCommons.add(new BotCommand("/start", "запустить бота"));
        listofCommons.add(new BotCommand("/help", "инфо о боте"));
        listofCommons.add(new BotCommand("/play_bot", "сыграть в крестики нолики с ботом"));
        listofCommons.add(new BotCommand("/play_user", "сыграть в крестики нолики с другим человеком"));
        listofCommons.add(new BotCommand("/join", "подключиться к комнате (введите ID комнаты)"));
        listofCommons.add(new BotCommand("/cancel", "прекратить игру"));

    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Update received: {}", update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.info("Received message: {} from chatId: {}", messageText, chatId);

            // Отправить всем пользователям сообщение (только для владельца бота)
            if (messageText.contains("/send") && botConfig.getBotOwnerId() == chatId) {
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepository.findAll();
                for (TgUser user : users) {
                    sendMessage(user.getChatId(), textToSend);
                }
                return;
            }

            // Обработка ходов в игре с ботом
            if (game != null && messageText.matches("[1-9]")) {
                handleBotGameMove(chatId, messageText);
                return;
            }

            // Обработка команды /join
            if (messageText.startsWith("/join")) {
                handleJoinCommand(chatId, messageText);
                return;
            }

            // Обработка команд
            switch (messageText) {

                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMessage(chatId, "Этот бот создан для игры в крестики нолики.\n\n" +
                            "Вы можете нажать на команды в главном меню слева или написать в чате:\n\n" +
                            "/play_bot чтобы сыграть с ботом\n\n" +
                            "/play_user чтобы найти человека и сыграть с ним.\n\n" +
                            "Для хода Вам нужно отправить число 1-9 для обозначения клетки хода.\n\n" +
                            "Чтобы прекратить игру отправьте команду /cancel");
                    break;

                case "/play_bot":
                    game = new Game();
                    game.initMap();
                    sendMessage(chatId, "Игра началась! Вы играете за Х. Первый ход за Вами.\n" +
                            "Введите число 1-9 для обозначения клетки хода.\n\n" + game.getMapAsString());
                    break;

                case "/play_user":
                    handlePlayUserCommand(chatId);
                    break;

                case "/cancel":

                    if (game != null) {
                        // Если игра с ботом
                        game = null;
                        sendMessage(chatId, EmojiParser.parseToUnicode("Игра с ботом прекращена. :x:"));
                    } else if (userToRoomMap.containsKey(chatId)) {

                        // Если игра с пользователем
                        String roomId = userToRoomMap.get(chatId);
                        GameRoom room = gameRooms.get(roomId);
                        sendMessage(room.getPlayer1(), EmojiParser.parseToUnicode("Игра с пользователем прекращена. :x:"));
                        sendMessage(room.getPlayer2(), EmojiParser.parseToUnicode("Игра с пользователем прекращена. :x:"));
                        cleanupRoom(roomId);

                    } else {
                        sendMessage(chatId, EmojiParser.parseToUnicode("Нет активной игры для отмены. :x:"));
                    }
                    break;


                default:
                    // Если команда не распознана
                    if (game == null && !userToRoomMap.containsKey(chatId)) {
                        String answer = EmojiParser.parseToUnicode("Такой команды не существует. :x:");
                        sendMessage(chatId, answer);
                    }
            }

            // Обработка ходов в игре с пользователем
            if (userToRoomMap.containsKey(chatId)) {
                handleUserGameMove(chatId, messageText);
            }
        }
    }

    private void handleBotGameMove(long chatId, String messageText) {

        if (game != null && messageText.matches("[1-9]")) {
            int cell = Integer.parseInt(messageText);
            int x = (cell - 1) % 3;
            int y = (cell - 1) / 3;

            if (game.makeMove(x, y, Game.X_FIELD)) {
                sendMessage(chatId, "Ваш ход:\n\n" + game.getMapAsString());

                if (game.checkWin(Game.X_FIELD)) {
                    String answer = EmojiParser.parseToUnicode("Игрок X выиграл! :tada: Игра окончена.");
                    sendMessage(chatId, answer);
                    game = null;

                } else if (game.checkDraw()) {
                    sendMessage(chatId, "Ничья! Игра окончена.");
                    game = null;

                } else {
                    game.botTurn(); // Ход бота
                    sendMessage(chatId, "Ход бота:\n\n" + game.getMapAsString());

                    if (game.checkWin(Game.O_FIELD)) {
                        String answer = EmojiParser.parseToUnicode("Бот выиграл!:broken_heart: Игра окончена.");
                        sendMessage(chatId, answer);
                        game = null;

                    } else if (game.checkDraw()) {
                        sendMessage(chatId, "Ничья! Игра окончена.");
                        game = null;
                    }
                }

            } else {
                String answer = EmojiParser.parseToUnicode("Некорректный ход. Попробуйте ещё раз. :broken_heart:");
                sendMessage(chatId, answer);
            }
        }
    }


    // Метод для обработки команды /play_user
    private void handlePlayUserCommand(long chatId) {
        if (userToRoomMap.containsKey(chatId)) {
            sendMessage(chatId, "Вы уже находитесь в комнате. Ожидайте второго игрока.");
            return;
        }

        String roomId = generateRoomId();
        GameRoom room = new GameRoom(chatId);
        gameRooms.put(roomId, room);
        userToRoomMap.put(chatId, roomId);

        sendMessage(chatId, "Комната создана. ID комнаты: "
                + roomId + "\n\nОтправьте ID вашему другу и он сможет подключиться к игре с помощью команды /join" +
                "\n\nЛибо используйте существующий ID для подключения к другой комнате.");
    }

    // Метод для обработки команды /join
    private void handleJoinCommand(long chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length != 2) {
            sendMessage(chatId, "Используйте команду /join [ID комнаты].");
            return;
        }

        String roomId = parts[1];
        if (!gameRooms.containsKey(roomId)) {
            sendMessage(chatId, "Комната с таким ID не найдена.");
            return;
        }

        GameRoom room = gameRooms.get(roomId);
        if (room.isFull()) {
            sendMessage(chatId, "Комната уже заполнена.");
            return;
        }

        room.setPlayer2(chatId);
        userToRoomMap.put(chatId, roomId);
        sendMessage(room.getPlayer1(), "Игрок подключился к комнате! Игра начинается. Вы ходите первым и играете за Х.\n\nДля хода введите число 1-9 для обозначения клетки хода.\n\n" + room.getGame().getMapAsString());
        sendMessage(room.getPlayer2(), "Вы подключились к комнате. Игра начинается! Вы ходите вторым и играете за О.\n\nДля хода введите число 1-9 для обозначения клетки хода.\n\n" + room.getGame().getMapAsString());
    }

    // Метод для обработки ходов в игре с пользователем
    private void handleUserGameMove(long chatId, String messageText) {

        String roomId = userToRoomMap.get(chatId);
        GameRoom room = gameRooms.get(roomId);

        if (messageText.matches("[1-9]")) {
            int cell = Integer.parseInt(messageText);
            int x = (cell - 1) % 3;
            int y = (cell - 1) / 3;

            if ((room.getCurrentPlayer() == 'X' && chatId == room.getPlayer1()) ||
                    (room.getCurrentPlayer() == 'O' && chatId == room.getPlayer2())) {

                if (room.getGame().makeMove(x, y, room.getCurrentPlayer())) {
                    sendMessage(room.getPlayer1(), "Ход сделан:\n\n" + room.getGame().getMapAsString());
                    sendMessage(room.getPlayer2(), "Ход сделан:\n\n" + room.getGame().getMapAsString());

                    if (room.getGame().checkWin(room.getCurrentPlayer())) {
                        sendMessage(room.getPlayer1(), "Игрок " + room.getCurrentPlayer() + " выиграл!");
                        sendMessage(room.getPlayer2(), "Игрок " + room.getCurrentPlayer() + " выиграл!");
                        cleanupRoom(roomId);
                    } else if (room.getGame().checkDraw()) {
                        sendMessage(room.getPlayer1(), "Ничья!");
                        sendMessage(room.getPlayer2(), "Ничья!");
                        cleanupRoom(roomId);
                    } else {
                        room.switchPlayer();
                    }
                } else {
                    sendMessage(chatId, "Некорректный ход. Попробуйте ещё раз.");
                }
            } else {
                sendMessage(chatId, "Сейчас не ваш ход.");
            }
        }
    }

    private String generateRoomId() {
        return UUID.randomUUID().toString().substring(0, 3);
    }

    private void cleanupRoom(String roomId) {
        GameRoom room = gameRooms.get(roomId);
        userToRoomMap.remove(room.getPlayer1());
        userToRoomMap.remove(room.getPlayer2());
        gameRooms.remove(roomId);
    }

//    Метод добавление пользователя в бд
    private void registerUser(Message msg) {

        if(userRepository.findById(msg.getChatId()).isEmpty()) {

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            TgUser user = new TgUser();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved " + user);
        }

    }

    private void startCommandReceived(long chatId, String name) {

        String answer = EmojiParser.parseToUnicode("Привет, " + name  + ", :blush:\n\n" +
                "Этот бот создан для игры в крестики-нолики.\n\n" +
                "Вы можете нажать на команды в главном меню слева или написав в чате:\n\n" +
                "/play_bot чтобы сыграть с ботом\n" +
                "/play_user чтобы получить ID для игры с другим пользователем\n" +
                "/join [ID] ввести ID для того чтобы игра с другим пользователем началась\n" +
                "/help если что-то осталось непонятным");

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.info("Message sent: {}", message.getText(), "To: {}", message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}