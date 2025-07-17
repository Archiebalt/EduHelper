package ie.arch.tutorbot.Service.manager.progress_control;

import static ie.arch.tutorbot.Service.data.CallbackData.*;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import ie.arch.tutorbot.Service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.Service.factory.KeyboardFactory;
import ie.arch.tutorbot.Service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProgressControlManager extends AbstractManager {

    AnswerMethodFactory methodFactory;
    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();

        switch (callbackData) {

            case PROGRESS -> {
                return mainMenu(callbackQuery);
            }

            case PROGRESS_STAT -> {
                return stat(callbackQuery);
            }

        }

        return null;
    }

    private BotApiMethod<?> stat(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,

                """
                        Здесь будет статистика
                        """,

                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(PROGRESS)));
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(message.getChatId(),

                """
                        Здесь вы можете увидеть
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)));
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,

                """
                        Здесь вы можете увидеть
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)));
    }
}
