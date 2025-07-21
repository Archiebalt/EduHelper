package ie.arch.tutorbot.service.handler;

import static ie.arch.tutorbot.service.data.CallbackData.*;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import ie.arch.tutorbot.service.manager.auth.AuthManager;
import ie.arch.tutorbot.service.manager.feedback.FeedbackManager;
import ie.arch.tutorbot.service.manager.help.HelpManager;
import ie.arch.tutorbot.service.manager.profile.ProfileManager;
import ie.arch.tutorbot.service.manager.progress_control.ProgressControlManager;
import ie.arch.tutorbot.service.manager.search.SearchManager;
import ie.arch.tutorbot.service.manager.task.TaskManager;
import ie.arch.tutorbot.service.manager.timetable.TimetableManager;
import ie.arch.tutorbot.telegram.Bot;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CallbackQueryHandler {

    HelpManager helpManager;

    FeedbackManager feedbackManager;

    TimetableManager timeTableManager;

    TaskManager taskManager;

    ProgressControlManager progressControlManager;

    AuthManager authManager;

    ProfileManager profileManager;

    SearchManager searchManager;

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String keyword = callbackData.split("_")[0];

        if (TIMETABLE.equals(keyword)) {
            return timeTableManager.answerCallbackQuery(callbackQuery, bot);
        }

        if (TASK.equals(keyword)) {
            return taskManager.answerCallbackQuery(callbackQuery, bot);
        }

        if (PROGRESS.equals(keyword)) {
            return progressControlManager.answerCallbackQuery(callbackQuery, bot);
        }

        if (AUTH.equals(keyword)) {
            return authManager.answerCallbackQuery(callbackQuery, bot);
        }

        if (PROFILE.equals(keyword)) {
            return profileManager.answerCallbackQuery(callbackQuery, bot);
        }

        if (SEARCH.equals(keyword)) {
            return searchManager.answerCallbackQuery(callbackQuery, bot);
        }

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
