package ie.arch.tutorbot.Service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.telegram.Bot;
import ie.arch.tutorbot.telegram.TelegramProperties;
import static ie.arch.tutorbot.Service.data.Command.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandHandler {

    TelegramProperties telegramProperties;

    public BotApiMethod<?> answer(Message message, Bot bot) {

        String command = message.getText();

        switch (command) {
            case START -> {
                return start(message);
            }

            case FEEDBACK -> {
                return feedback(message);
            }

            case HELP -> {
                return help(message);
            }

            default -> {
                return defaultAnswer(message);
            }
        }

    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("Неподдерживаемая команда")
                .build();
    }

    private BotApiMethod<?> help(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("""

                        📍 Доступные команды:
                        - start
                        - help
                        - feedback

                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости

                                                """)
                .build();
    }

    private BotApiMethod<?> feedback(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("""

                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/Archiebalt
                        Telegram - https://t.me/Archie1810

                                                            """)
                .disableWebPagePreview(true)
                .build();
    }

    private BotApiMethod<?> start(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("""

                        🖖Приветствую в Tutor-Bot, инструменте для упрощения взаимодействия репититора и ученика.

                        Что бот умеет?
                        📌 Составлять расписание
                        📌 Прикреплять домашние задания
                        📌 Ввести контроль успеваемости

                                """)

                .build();
    }

}
