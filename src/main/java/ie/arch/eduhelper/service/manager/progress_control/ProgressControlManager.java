package ie.arch.tutorbot.service.manager.progress_control;

import static ie.arch.tutorbot.service.data.CallbackData.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.entity.task.CompleteStatus;
import ie.arch.tutorbot.entity.user.Role;
import ie.arch.tutorbot.entity.user.User;
import ie.arch.tutorbot.repository.TaskRepo;
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
public class ProgressControlManager extends AbstractManager {

    AnswerMethodFactory methodFactory;

    KeyboardFactory keyboardFactory;

    TaskRepo taskRepo;

    UserRepo userRepo;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());

        if (Role.STUDENT.equals(user.getRole())) {
            return null;
        }

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

        String[] splitCallbackData = callbackData.split("_");

        switch (splitCallbackData[1]) {
            case USER -> {
                return showUserStat(callbackQuery, splitCallbackData[2]);
            }
        }

        return null;
    }

    private BotApiMethod<?> showUserStat(CallbackQuery callbackQuery, String id) {
        var student = userRepo.findUserByToken(id);
        var details = student.getDetails();
        StringBuilder text = new StringBuilder("\uD83D\uDD39 Статистика по ученику \"")
                .append(details.getFirstname() + "(" + details.getUsername() + ")")
                .append("\"")
                .append("\n\n");

        int success = taskRepo.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.SUCCESS);

        int fail = taskRepo.countAllByUsersContainingAndIsFinishedAndCompleteStatus(
                student, true, CompleteStatus.FAIL);

        int sum = fail + success;

        text.append("\uD83D\uDCCD Решено - ")
                .append(success);
        text.append("\n\uD83D\uDCCD Провалено - ")
                .append(fail);
        text.append("\n\uD83D\uDCCD Всего - ")
                .append(sum);

        return methodFactory.getEditMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDD19 Назад"),
                        List.of(1),
                        List.of(PROGRESS_STAT)));
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        Здесь вы можете увидеть статистику по каждому ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDCCA Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)));
    }

    private BotApiMethod<?> mainMenu(Message message) {
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        Здесь вы можете увидеть статистику по каждому ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("\uD83D\uDCCA Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)));
    }

    private BotApiMethod<?> stat(CallbackQuery callbackQuery) {
        var teacher = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        List<User> students = teacher.getUsers();
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        int index = 0;

        for (User student : students) {
            text.add(student.getDetails().getFirstname());
            data.add(PROGRESS_USER + student.getToken());
            if (index == 4) {
                cfg.add(index);
                index = 0;
            } else {
                index++;
            }
        }

        if (index != 0) {
            cfg.add(index);
        }

        data.add(PROGRESS);
        text.add("\uD83D\uDD19 Назад");
        cfg.add(1);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "\uD83D\uDC64 Выберете ученика",
                keyboardFactory.getInlineKeyboard(
                        text, cfg, data));
    }
}
