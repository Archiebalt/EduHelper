package ie.arch.tutorbot.Service.manager;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.Service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.Service.factory.KeyboardFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HelpManager {

    AnswerMethodFactory answerMethodFactory;
    KeyboardFactory keyboardFactory;

    public BotApiMethod<?> answerCommand(Message message) {

        return answerMethodFactory.getSendMessage(
                message.getChatId(),

                """
                        📍 Доступные команды:
                        - start
                        - help
                        - feedback

                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости

                        """,

                null);

    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery) {

        return answerMethodFactory.getEditMessageText(callbackQuery,
                """
                        📍 Доступные команды:
                        - start
                        - help
                        - feedback

                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости

                        """,
                null);
                
    }

}
