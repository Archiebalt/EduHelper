package ie.arch.eduhelper.service.manager.profile;

import static ie.arch.eduhelper.service.data.CallbackData.PROFILE_REFRESH_TOKEN;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.eduhelper.repository.UserRepo;
import ie.arch.eduhelper.service.factory.AnswerMethodFactory;
import ie.arch.eduhelper.service.factory.KeyboardFactory;
import ie.arch.eduhelper.service.manager.AbstractManager;
import ie.arch.eduhelper.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProfileManager extends AbstractManager {

    AnswerMethodFactory methodFactory;

    UserRepo userRepo;

    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return showProfile(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        switch (callbackQuery.getData()) {
            case PROFILE_REFRESH_TOKEN -> {
                return refreshToken(callbackQuery);
            }

            default -> {
                return null;
            }
        }
    }

    private BotApiMethod<?> showProfile(Message message) {
        Long chatId = message.getChatId();

        return methodFactory.getSendMessage(
                chatId,
                getProfileText(chatId).toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(PROFILE_REFRESH_TOKEN)));
    }

    private BotApiMethod<?> showProfile(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();

        return methodFactory.getEditMessageText(
                callbackQuery,
                getProfileText(chatId),
                keyboardFactory.getInlineKeyboard(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(PROFILE_REFRESH_TOKEN)));
    }

    private String getProfileText(Long chatId) {
        StringBuilder text = new StringBuilder("\uD83D\uDC64 Профиль\n");
        var user = userRepo.findById(chatId).orElseThrow();
        var details = user.getDetails();

        if (details.getUsername() == null) {
            text.append("\u25AA Имя пользователя - ").append(details.getUsername());
        } else {
            text.append("\u25AA Имя пользователя - ").append(details.getFirstname());

        }

        text.append("\n\u25AA Роль - ").append(user.getRole().name());
        text.append("\n\u25AA Ваш уникальный токен - ");
        text.append(user.getToken().toString());
        text.append("\n\n⚠️ - токен необходим для того, чтобы ученик или преподаватель могли устанавливать между собой связь");

        return text.toString();
    }

    private BotApiMethod<?> refreshToken(CallbackQuery callbackQuery) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.refreshToken();
        userRepo.save(user);

        return showProfile(callbackQuery);
    }

}
