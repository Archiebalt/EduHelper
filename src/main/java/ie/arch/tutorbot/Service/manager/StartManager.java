package ie.arch.tutorbot.Service.manager;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.Service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.Service.factory.KeyboardFactory;

import static ie.arch.tutorbot.Service.data.CallbackData.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StartManager {

    AnswerMethodFactory answerMethodFactory;
    KeyboardFactory keyboardFactory;

    public SendMessage answerCommand(Message message) {

        return answerMethodFactory.getSendMessage(
                message.getChatId(),

                """

                        🖖Приветствую в Tutor-Bot, инструменте для упрощения взаимодействия репититора и ученика.

                        Что бот умеет?
                        📌 Составлять расписание
                        📌 Прикреплять домашние задания
                        📌 Ввести контроль успеваемости

                                """,

                keyboardFactory.getInlineKeyboard(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP, FEEDBACK)));

    }

}
