package ie.arch.eduhelper.service.manager.search;

import static ie.arch.eduhelper.service.data.CallbackData.SEARCH_CANCEL;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ie.arch.eduhelper.entity.user.Action;
import ie.arch.eduhelper.entity.user.Role;
import ie.arch.eduhelper.entity.user.User;
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
public class SearchManager extends AbstractManager {

    AnswerMethodFactory methodFactory;
    KeyboardFactory keyboardFactory;
    UserRepo userRepo;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return askToken(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        try {
            bot.execute(methodFactory.getDeleteMessage(message.getChatId(), message.getMessageId() - 1));
        } catch (TelegramApiException exc) {
            log.error(exc.getMessage());
        }

        var user = userRepo.findUserByChatId(message.getChatId());

        switch (user.getAction()) {
            case SENDING_TOKEN -> {
                return checkToken(message, user);
            }

            default -> {
                return null;
            }
        }
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        switch (callbackQuery.getData()) {
            case SEARCH_CANCEL -> {
                try {
                    return cancel(callbackQuery, bot);
                } catch (TelegramApiException exc) {
                    log.error(exc.getMessage());
                }
            }
        }

        return null;
    }

    private BotApiMethod<?> checkToken(Message message, User user) {
        String token = message.getText();
        var userSecond = userRepo.findUserByToken(token);

        if (userSecond == null) {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    "Пользователя с таким токеном не существует",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Отмена операции"),
                            List.of(1),
                            List.of(SEARCH_CANCEL)));
        }

        if (validation(user, userSecond)) {
            if (user.getRole() == Role.TEACHER) {
                user.addUser(userSecond);
            } else {
                userSecond.addUser(user);
            }

            user.setAction(Action.FREE);
            userRepo.save(user);
            userRepo.save(userSecond);

            return methodFactory.getSendMessage(message.getChatId(), "Связь успешно установлена", null);
        }

        return methodFactory.getSendMessage(
                    message.getChatId(),
                    "Нельзя установить связи: учитель-учитель или ученик-ученик",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Отмена операции"),
                            List.of(1),
                            List.of(SEARCH_CANCEL)));

    }

    private boolean validation(User userFirst, User userSecond) {
        return userFirst.getRole() != userSecond.getRole();
    }

    private BotApiMethod<?> cancel(CallbackQuery callbackQuery, Bot bot) throws TelegramApiException {
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepo.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepo.save(user);

        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Операция отменена успешно"));

        return methodFactory.getDeleteMessage(
                chatId,
                callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> askToken(Message message) {
        Long chatId = message.getChatId();
        var user = userRepo.findUserByChatId(chatId);
        user.setAction(Action.SENDING_TOKEN);
        userRepo.save(user);

        return methodFactory.getSendMessage(
                chatId,
                "Отправьте токен",
                keyboardFactory.getInlineKeyboard(
                        List.of("Отмена"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)));
    }

}
