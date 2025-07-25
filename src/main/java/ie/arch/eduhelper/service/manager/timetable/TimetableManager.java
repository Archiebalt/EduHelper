package ie.arch.eduhelper.service.manager.timetable;

import static ie.arch.eduhelper.service.data.CallbackData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ie.arch.eduhelper.entity.timetable.Timetable;
import ie.arch.eduhelper.entity.timetable.WeekDay;
import ie.arch.eduhelper.entity.user.Action;
import ie.arch.eduhelper.entity.user.Role;
import ie.arch.eduhelper.entity.user.User;
import ie.arch.eduhelper.repository.DetailsRepo;
import ie.arch.eduhelper.repository.TimetableRepo;
import ie.arch.eduhelper.repository.UserRepo;
import ie.arch.eduhelper.service.factory.AnswerMethodFactory;
import ie.arch.eduhelper.service.factory.KeyboardFactory;
import ie.arch.eduhelper.service.manager.AbstractManager;
import ie.arch.eduhelper.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TimetableManager extends AbstractManager {

    AnswerMethodFactory methodFactory;

    KeyboardFactory keyboardFactory;

    UserRepo userRepo;

    DetailsRepo detailsRepo;

    TimetableRepo timetableRepo;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());
        try {
            bot.execute(methodFactory.getDeleteMessage(message.getChatId(), message.getMessageId() - 1));
            bot.execute(methodFactory.getSendMessage(message.getChatId(), "Значение успешно установлено", null));
        } catch (TelegramApiException exc) {
            log.error(exc.getMessage());
        }

        switch (user.getAction()) {
            case SENDING_TITLE -> {
                return setTitle(message, user);
            }

            case SENDING_DESCRIPTION -> {
                return setDescription(message, user);
            }

            default -> {
                return null;
            }
        }
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String[] splitCallbackData = callbackData.split("_");

        if (splitCallbackData.length > 1 && "add".equals(splitCallbackData[1])) {

            if (splitCallbackData.length == 2 || splitCallbackData.length == 3) {
                return add(callbackQuery, splitCallbackData);
            }

            switch (splitCallbackData[2]) {
                case WEEKDAY -> {
                    return addWeekDay(callbackQuery, splitCallbackData);
                }
                case HOUR -> {
                    return addHour(callbackQuery, splitCallbackData);
                }
                case MINUTE -> {
                    return addMinute(callbackQuery, splitCallbackData);
                }
                case USER -> {
                    return addUser(callbackQuery, splitCallbackData);
                }
                case TITLE -> {
                    return askTitle(callbackQuery, splitCallbackData);
                }
                case DESCRIPTION -> {
                    return askDescription(callbackQuery, splitCallbackData);
                }
            }

        }

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
            case TIMETABLE_1, TIMETABLE_2, TIMETABLE_3,
                    TIMETABLE_4, TIMETABLE_5, TIMETABLE_6,
                    TIMETABLE_7 -> {
                return showDay(callbackQuery);
            }
        }

        if (FINISH.equals(splitCallbackData[1])) {
            try {
                return finish(callbackQuery, splitCallbackData, bot);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }

        if (BACK.equals(splitCallbackData[1])) {
            return back(callbackQuery, splitCallbackData);
        }

        if (splitCallbackData.length > 2 && REMOVE.equals(splitCallbackData[1])) {
            switch (splitCallbackData[2]) {
                case WEEKDAY -> {
                    return removeWeekday(callbackQuery, splitCallbackData[3]);
                }
                case POS -> {
                    return askConfirmation(callbackQuery, splitCallbackData);
                }
                case FINAL -> {
                    try {
                        return deleteTimetable(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    private BotApiMethod<?> deleteTimetable(CallbackQuery callbackQuery, String id, Bot bot)
            throws TelegramApiException {

        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        timetable.setUsers(null);
        timetableRepo.delete(timetable);
        bot.execute(methodFactory.getAnswerCallbackQuery(callbackQuery.getId(),
                "Запись \"" + timetable.getTitle() + "\" успешно удалена"));

        return methodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String[] splitCallbackData) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Вы уверены, что хотите удалить запись?",
                keyboardFactory.getInlineKeyboard(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TIMETABLE_REMOVE_FINAL + splitCallbackData[3],
                                TIMETABLE_REMOVE_WEEKDAY + splitCallbackData[4])));
    }

    private BotApiMethod<?> removeWeekday(CallbackQuery callbackQuery, String number) {
        WeekDay weekDay = WeekDay.MONDAY;
        switch (number) {
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;
        }
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        for (Timetable timeTable : timetableRepo.findAllByUsersContainingAndWeekDay(
                userRepo.findUserByChatId(callbackQuery.getMessage().getChatId()),
                weekDay)) {
            data.add(TIMETABLE_REMOVE_POS + timeTable.getId() + "_" + number);
            text.add(timeTable.getTitle() + " " + timeTable.getHour() + ":" + timeTable.getMinute());
            cfg.add(1);
        }
        cfg.add(1);
        data.add(TIMETABLE_REMOVE);
        text.add("Назад");
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете занятие которое хотите убрать из расписания",
                keyboardFactory.getInlineKeyboard(
                        text, cfg, data));
    }

    private BotApiMethod<?> askDescription(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_DESCRIPTION);
        var details = user.getDetails();
        details.setTimetableId(id);
        detailsRepo.save(details);
        user.setDetails(details);
        userRepo.save(user);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите описание",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)));
    }

    private BotApiMethod<?> askTitle(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TITLE);
        var details = user.getDetails();
        details.setTimetableId(id);
        detailsRepo.save(details);
        user.setDetails(details);
        userRepo.save(user);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите загаловок",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)));
    }

    private BotApiMethod<?> setDescription(Message message, User user) {
        user.setAction(Action.FREE);
        userRepo.save(user);
        String id = user.getDetails().getTimetableId();
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        timetable.setDescription(message.getText());
        timetableRepo.save(timetable);

        return back(message, id);
    }

    private BotApiMethod<?> setTitle(Message message, User user) {
        user.setAction(Action.FREE);
        userRepo.save(user);
        String id = user.getDetails().getTimetableId();
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        timetable.setTitle(message.getText());
        timetableRepo.save(timetable);

        return back(message, id);
    }

    private BotApiMethod<?> back(Message message, String id) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Настройте заголовок и описание",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить загаловок",
                                "Изменить описание",
                                "Завершить"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id)));
    }

    private BotApiMethod<?> back(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.FREE);
        userRepo.save(user);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Настройте заголовок и описание",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить загаловок",
                                "Изменить описание",
                                "Завершить"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id)));
    }

    private BotApiMethod<?> finish(CallbackQuery callbackQuery, String[] splitCallbackData, Bot bot)
            throws TelegramApiException {
        String id = splitCallbackData[2];
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);

        timetable.setInCreation(false);
        timetableRepo.save(timetable);

        bot.execute(methodFactory.getAnswerCallbackQuery(callbackQuery.getId(),
                "Процес создание записи в расписание успешно завершен"));

        return methodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        var user = userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3]));
        timetable.addUser(user);
        timetable.setTitle(user.getDetails().getFirstname());
        timetableRepo.save(timetable);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Успешно! Запись добавлена, теперь вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить заголовок", "Изменить описание",
                                "Завершить создание"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id)));
    }

    private BotApiMethod<?> addMinute(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        timetable.setMinute(Short.valueOf(splitCallbackData[3]));
        int index = 0;
        var me = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());

        for (User user : me.getUsers()) {

            text.add(user.getDetails().getFirstname());
            data.add(TIMETABLE_ADD_USER + user.getChatId() + "_" + id);
            if (index == 5) {
                cfg.add(5);
                index = 0;
            } else {
                index += 1;
            }
        }

        if (index != 0) {
            cfg.add(index);
        }
        cfg.add(1);
        data.add(TIMETABLE_ADD_HOUR + timetable.getHour() + "_" + id);
        text.add("Назад");

        timetableRepo.save(timetable);

        String messageText = "Выберете ученика";
        if (cfg.size() == 1) {
            messageText = "У вас нет ни одного ученика";
        }

        return methodFactory.getEditMessageText(
                callbackQuery,
                messageText,
                keyboardFactory.getInlineKeyboard(text, cfg, data));
    }

    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        timetable.setHour(Short.valueOf(splitCallbackData[3]));

        for (int i = 0; i <= 59; i++) {
            text.add(String.valueOf(i));
            data.add(TIMETABLE_ADD_MINUTE + i + "_" + id);
        }

        text.add("\uD83D\uDD19 Назад");

        switch (timetable.getWeekDay()) {
            case MONDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 1 + "_" + id);
            case TUESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 2 + "_" + id);
            case WEDNESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 3 + "_" + id);
            case THURSDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 4 + "_" + id);
            case FRIDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 5 + "_" + id);
            case SATURDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 6 + "_" + id);
            case SUNDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 7 + "_" + id);
        }

        timetableRepo.save(timetable);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете минуту",
                keyboardFactory.getInlineKeyboard(
                        text,
                        List.of(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1),
                        data));
    }

    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] data) {
        String id = data[4];
        List<Timetable> possibleMatches = timetableRepo.findByIdStartingWith(id);
        var timetable = possibleMatches.get(0);

        switch (data[3]) {
            case "1" -> timetable.setWeekDay(WeekDay.MONDAY);
            case "2" -> timetable.setWeekDay(WeekDay.TUESDAY);
            case "3" -> timetable.setWeekDay(WeekDay.WEDNESDAY);
            case "4" -> timetable.setWeekDay(WeekDay.THURSDAY);
            case "5" -> timetable.setWeekDay(WeekDay.FRIDAY);
            case "6" -> timetable.setWeekDay(WeekDay.SATURDAY);
            case "7" -> timetable.setWeekDay(WeekDay.SUNDAY);
        }

        List<String> buttonsData = new ArrayList<>();
        List<String> text = new ArrayList<>();

        for (int i = 1; i <= 24; i++) {
            text.add(String.valueOf(i));
            buttonsData.add(TIMETABLE_ADD_HOUR + i + "_" + data[4]);
        }

        buttonsData.add(TIMETABLE_ADD + "_" + data[4]);
        text.add("\uD83D\uDD19 Назад");
        timetableRepo.save(timetable);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете час",
                keyboardFactory.getInlineKeyboard(
                        text,
                        List.of(6, 6, 6, 6, 1),
                        buttonsData));
    }

    private BotApiMethod<?> showDay(CallbackQuery callbackQuery) {
        var currentUser = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        WeekDay weekDay = WeekDay.MONDAY;

        switch (callbackQuery.getData().split("_")[1]) {
            case "1" -> weekDay = WeekDay.MONDAY;
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;
        }

        List<Timetable> timetableList = timetableRepo.findAllByUsersContainingAndWeekDay(currentUser, weekDay);
        StringBuilder text = new StringBuilder();

        if (timetableList == null || timetableList.isEmpty()) {
            text.append("У вас нет занятий в этот день");
        } else {
            text.append("📅 Ваши занятия на ").append(weekDay.getDisplayName()).append(":\n\n");

            for (Timetable t : timetableList) {
                // Для ученика показываем преподавателей, для преподавателя - учеников
                List<User> otherUsers = t.getUsers().stream()
                        .filter(u -> !u.equals(currentUser))
                        .collect(Collectors.toList());

                text.append("\uD83D\uDD52 ")
                        .append(String.format("%02d:%02d", t.getHour(), t.getMinute()))
                        .append(" - ")
                        .append(t.getTitle());

                if (!otherUsers.isEmpty()) {
                    String roleLabel = currentUser.getRole() == Role.STUDENT ? "👨‍🏫 Преподаватель" : "👨🎓 Ученик";
                    text.append("\n   ").append(roleLabel).append(": ");

                    List<String> userNames = new ArrayList<>();
                    for (User user : otherUsers) {
                        String name = user.getDetails().getFirstname();
                        if (user.getDetails().getUsername() != null && !user.getDetails().getUsername().isEmpty()) {
                            name += " (@" + user.getDetails().getUsername() + ")";
                        }
                        userNames.add(name);
                    }

                    text.append(String.join(", ", userNames));
                }

                text.append("\n\n");
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
                    "📆 Здесь вы можете увидеть ваше расписание",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)));
        }

        return methodFactory.getSendMessage(
                message.getChatId(),
                "📆 Здесь вы можете управлять вашим расписанием",
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
                    "📆 Здесь вы можете увидеть ваше расписание",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)));
        }

        return methodFactory.getEditMessageText(
                callbackQuery,
                "📆 Здесь вы можете управлять вашим расписанием",
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое расписание", "Удалить занятие", "Добавить занятие"),
                        List.of(1, 2),
                        List.of(TIMETABLE_SHOW, TIMETABLE_REMOVE, TIMETABLE_ADD)));
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = "";

        if (splitCallbackData.length == 2) {
            var timetable = new Timetable();
            timetable.addUser(userRepo.findUserByChatId(callbackQuery.getMessage().getChatId()));
            timetable.setInCreation(true);
            id = timetableRepo.save(timetable).getId().toString().substring(0, 8);
        } else {
            id = splitCallbackData[2];
        }

        List<String> data = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            data.add(TIMETABLE_ADD_WEEKDAY + i + "_" + id);
        }

        data.add(TIMETABLE);

        return methodFactory.getEditMessageText(callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС", "Назад"),
                        List.of(7, 1),
                        data));
    }

    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        List<String> data = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            data.add(TIMETABLE_REMOVE_WEEKDAY + i);
        }

        data.add(TIMETABLE);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "✂️ Выберете день",
                keyboardFactory.getInlineKeyboard(
                        List.of("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС", "Назад"),
                        List.of(7, 1),
                        data));
    }

    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(callbackQuery,
                "📆 Выберете день недели",
                keyboardFactory.getInlineKeyboard(
                        List.of(
                                "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС",
                                "Назад"),
                        List.of(7, 1),
                        List.of(TIMETABLE_1,
                                TIMETABLE_2,
                                TIMETABLE_3,
                                TIMETABLE_4,
                                TIMETABLE_5,
                                TIMETABLE_6,
                                TIMETABLE_7,
                                TIMETABLE)));
    }
}