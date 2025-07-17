package ie.arch.tutorbot.Service.handler;

import static ie.arch.tutorbot.Service.data.CallbackData.*;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import ie.arch.tutorbot.telegram.Bot;
import ie.arch.tutorbot.Service.manager.feedback.FeedbackManager;
import ie.arch.tutorbot.Service.manager.help.HelpManager;
import ie.arch.tutorbot.Service.manager.progress_control.ProgressControlManager;
import ie.arch.tutorbot.Service.manager.task.TaskManager;
import ie.arch.tutorbot.Service.manager.timetable.TimetableManager;

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
