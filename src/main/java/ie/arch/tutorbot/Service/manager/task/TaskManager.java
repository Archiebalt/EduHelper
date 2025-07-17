package ie.arch.tutorbot.Service.manager.task;

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
public class TaskManager extends AbstractManager {

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
            case TASK -> {
                return mainMenu(callbackQuery);
            }

            case TASK_CREATE -> {
                return create(callbackQuery);
            }
        }

        return null;
    }

    private BotApiMethod<?> create(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK)));
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(message.getChatId(),

                """
                        🗂 Вы можете добавить домашнее задание вашему ученику
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)));
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,

                """
                        🗂 Вы можете добавить домашнее задание вашему ученику
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)));
    }

}
