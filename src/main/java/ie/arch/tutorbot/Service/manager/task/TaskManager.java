package ie.arch.tutorbot.service.manager.task;

import static ie.arch.tutorbot.service.data.CallbackData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ie.arch.tutorbot.entity.task.Task;
import ie.arch.tutorbot.entity.user.Action;
import ie.arch.tutorbot.entity.user.User;
import ie.arch.tutorbot.repository.TaskRepo;
import ie.arch.tutorbot.repository.UserRepo;
import ie.arch.tutorbot.service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.service.factory.KeyboardFactory;
import ie.arch.tutorbot.service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TaskManager extends AbstractManager {

    AnswerMethodFactory methodFactory;

    KeyboardFactory keyboardFactory;

    UserRepo userRepo;

    TaskRepo taskRepo;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();
        var user = userRepo.findUserByChatId(message.getChatId());
        try {
            bot.execute(methodFactory.getDeleteMessage(chatId, message.getMessageId() - 1));
        } catch (TelegramApiException exc) {
            log.error(exc.getMessage());
        }

        switch (user.getAction()) {
            case SENDING_TASK -> {
                return addTask(message, chatId, user);
            }
        }

        return null;
    }

    @Transactional
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

        String[] splitCallbackData = callbackData.split("_");

        if (splitCallbackData.length > 2) {
            String keyWord = splitCallbackData[2];

            switch (keyWord) {
                case USER -> {
                    return setUser(callbackQuery, splitCallbackData);
                }

                case CANCEL -> {
                    try {
                        return abortCreation(callbackQuery, splitCallbackData[3], bot);
                    } catch (TelegramApiException exc) {
                        log.error(exc.getMessage());
                    }
                }
            }
        }

        return null;
    }

    private BotApiMethod<?> abortCreation(CallbackQuery callbackQuery, String id, Bot bot) throws TelegramApiException {
        taskRepo.deleteById(UUID.fromString(id));
        bot.execute(methodFactory.getAnswerCallbackQuery(callbackQuery.getId(), "Сообщение отменено"));
        return methodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> addTask(Message message, Long chatId, User user) {
        var task = taskRepo.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setMessageId(message.getMessageId());
        taskRepo.save(task);

        String id = String.valueOf(task.getId());

        user.setAction(Action.FREE);
        userRepo.save(user);

        return methodFactory.getSendMessage(
                chatId,
                "Отредактируйте ваше сообщение и нажмите",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить текст",
                                "Изменить медиа",
                                "Выбрать ученика",
                                "Отправить",
                                "Отмена"),
                        List.of(2, 1, 2),
                        List.of(
                                TASK_CREATE_TEXT + id,
                                TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER + id,
                                TASK_CREATE_SEND + id,
                                TASK_CREATE_CANCEL + id)));
    }

    private BotApiMethod<?> setUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());

        taskRepo.deleteByUsersContainingAndIsInCreation(user, true);

        taskRepo.save(
                Task.builder()
                        .users(List.of(
                                userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3])),
                                user))
                        .isInCreation(true)
                        .build());

        user.setAction(Action.SENDING_TASK);
        userRepo.save(user);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Отправьте задание одним сообщением",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_CREATE)));
    }

    private BotApiMethod<?> create(CallbackQuery callbackQuery) {
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();

        var teacher = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;

        for (User student : teacher.getUsers()) {
            text.add(student.getDetails().getFirstname());
            data.add(TASK_CREATE_USER + student.getChatId());
            if (index == 4) {
                cfg.add(index);
            } else {
                index++;
            }
        }

        if (index != 0) {
            cfg.add(index);
        }

        data.add(TASK);
        text.add("Назад");
        cfg.add(1);

        return methodFactory.getEditMessageText(callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание
                        """,
                keyboardFactory.getInlineKeyboard(
                        text, cfg, data));
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
