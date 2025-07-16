package ie.arch.tutorbot.Service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import ie.arch.tutorbot.Service.manager.feedback.FeedbackManager;
import ie.arch.tutorbot.Service.manager.help.HelpManager;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static ie.arch.tutorbot.Service.data.CallbackData.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CallbackQueryHandler {

    HelpManager helpManager;
    FeedbackManager feedbackManager;

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();

        switch (callbackData) {
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery, bot);
            }

            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
        }

        return null;
    }

}
