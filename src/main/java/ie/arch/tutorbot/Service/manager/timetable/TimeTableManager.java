package ie.arch.tutorbot.Service.manager.timetable;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.Service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.Service.factory.KeyboardFactory;
import ie.arch.tutorbot.Service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;

import static ie.arch.tutorbot.Service.data.CallbackData.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimetableManager extends AbstractManager {

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
            case TIMETABLE -> {
                return mainMenu(callbackQuery);
            }

            case TIMETABLE_SHOW -> {
                return show(callbackQuery);
            }

            case TIMETABLE_REMOVE -> {
                return remove(callbackQuery);
            }

            case TIMETABLE_ADD -> {
                return add(callbackQuery);
            }

        }

        return null;
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(message.getChatId(),

                """
                        📆 Здесь вы можете управлять вашим расписанием
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое расписание", "Удалить занятие", "Добавить занятие"),
                        List.of(1, 2),
                        List.of(TIMETABLE_SHOW, TIMETABLE_REMOVE, TIMETABLE_ADD)));
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,

                """
                        📆 Здесь вы можете управлять вашим расписанием
                                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое расписание", "Удалить занятие", "Добавить занятие"),
                        List.of(1, 2),
                        List.of(TIMETABLE_SHOW, TIMETABLE_REMOVE, TIMETABLE_ADD)));
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)));
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,
                """
                        ✂️ Выберете занятие, которое хотите удалить из вашего расписания
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)));
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,
                """
                        📆 Выберете день недели
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)));
    }

}
