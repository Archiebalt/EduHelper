package ie.arch.tutorbot.service.manager.auth;

import static ie.arch.tutorbot.service.data.CallbackData.AUTH_STUDENT;
import static ie.arch.tutorbot.service.data.CallbackData.AUTH_TEACHER;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ie.arch.tutorbot.entity.user.Action;
import ie.arch.tutorbot.entity.user.Role;
import ie.arch.tutorbot.repository.UserRepo;
import ie.arch.tutorbot.service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.service.factory.KeyboardFactory;
import ie.arch.tutorbot.service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthManager extends AbstractManager {

    UserRepo userRepo;

    AnswerMethodFactory methodFactory;

    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        Long chatId = message.getChatId();

        var user = userRepo.findById(chatId).orElseThrow();
        user.setAction(Action.AUTH);
        userRepo.save(user);

        return methodFactory.getSendMessage(
                chatId,

                """
                        Выберете свою роль
                        """,

                keyboardFactory.getInlineKeyboard(
                        List.of("Ученик", "Учитель"),
                        List.of(2),
                        List.of(AUTH_STUDENT, AUTH_TEACHER)));
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        var user = userRepo.findById(chatId).orElseThrow();

        if (AUTH_TEACHER.equals(callbackQuery.getData())) {
            user.setRole(Role.TEACHER);
        } else {
            user.setRole(Role.STUDENT);
        }

        user.setAction(Action.FREE);
        userRepo.save(user);

        try {
            bot.execute(
                    methodFactory.getAnswerCallbackQuery(
                            callbackQuery.getId(),
                            """
                                    Авторизация прошла успешна, повторите предыдущий запрос!"""));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return methodFactory.getDeleteMessage(
                chatId,
                messageId);
    }

}
