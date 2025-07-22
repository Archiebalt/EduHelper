package ie.arch.tutorbot.service.manager.timetable;

import static ie.arch.tutorbot.service.data.CallbackData.*;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.entity.timetable.Timetable;
import ie.arch.tutorbot.entity.timetable.WeekDay;
import ie.arch.tutorbot.entity.user.Role;
import ie.arch.tutorbot.repository.TimetableRepo;
import ie.arch.tutorbot.repository.UserRepo;
import ie.arch.tutorbot.service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.service.factory.KeyboardFactory;
import ie.arch.tutorbot.service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimetableManager extends AbstractManager {

    AnswerMethodFactory methodFactory;

    KeyboardFactory keyboardFactory;

    UserRepo userRepo;

    TimetableRepo timetableRepo;

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

            case TIMETABLE_1, TIMETABLE_2,
                    TIMETABLE_3, TIMETABLE_4,
                    TIMETABLE_5, TIMETABLE_6,
                    TIMETABLE_7 -> {
                return showDay(callbackQuery);
            }

        }

        return null;
    }

    private BotApiMethod<?> showDay(CallbackQuery callbackQuery) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        WeekDay weekDay = WeekDay.MONDAY;

        switch (callbackQuery.getData().split("_")[1]) {
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;
        }

        List<Timetable> timetableList = timetableRepo.findAllByUsersContainingAndWeekday(user, weekDay);
        StringBuilder text = new StringBuilder();

        if (timetableList == null || timetableList.isEmpty()) {
            text.append("У вас нет занятий в этот день");
        } else {
            text.append("У вас сегодня есть занятия:\n");

            for (Timetable t : timetableList) {
                text.append("\u25AA ")
                        .append(t.getHour())
                        .append(":")
                        .append(t.getMinute())
                        .append(" - ")
                        .append(t.getTitle())
                        .append("\n");

            }
        }

        return methodFactory.getEditMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)));
    }

    private BotApiMethod<?> mainMenu(Message message) {
        var user = userRepo.findUserByChatId(message.getChatId());

        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете увидеть ваше расписание
                                            """,
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)));
        }

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
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());

        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getEditMessageText(
                    callbackQuery,
                    """
                            📆 Здесь вы можете увидеть ваше расписание
                                            """,
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)));
        }

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
                        List.of(
                                "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС",
                                "Назад"),
                        List.of(7, 1),
                        List.of(
                                TIMETABLE_1, TIMETABLE_2, TIMETABLE_3, TIMETABLE_4, TIMETABLE_5, TIMETABLE_6,
                                TIMETABLE_7,
                                TIMETABLE)));
    }

}
