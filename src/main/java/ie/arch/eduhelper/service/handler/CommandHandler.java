package ie.arch.eduhelper.service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import static ie.arch.eduhelper.service.data.Command.*;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.eduhelper.service.manager.feedback.FeedbackManager;
import ie.arch.eduhelper.service.manager.help.HelpManager;
import ie.arch.eduhelper.service.manager.profile.ProfileManager;
import ie.arch.eduhelper.service.manager.progress_control.ProgressControlManager;
import ie.arch.eduhelper.service.manager.search.SearchManager;
import ie.arch.eduhelper.service.manager.start.StartManager;
import ie.arch.eduhelper.service.manager.task.TaskManager;
import ie.arch.eduhelper.service.manager.timetable.TimetableManager;
import ie.arch.eduhelper.telegram.Bot;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandHandler {

    StartManager startManager;

    FeedbackManager feedbackManager;

    HelpManager helpManager;

    TimetableManager timeTableManager;

    TaskManager taskManager;

    ProgressControlManager progressControlManager;

    ProfileManager profileManager;

    SearchManager searchManager;

    public BotApiMethod<?> answer(Message message, Bot bot) {

        String command = message.getText();

        switch (command) {
            case START -> {
                return startManager.answerCommand(message, bot);
            }

            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message, bot);
            }

            case HELP_COMMAND -> {
                return helpManager.answerCommand(message, bot);
            }

            case TIMETABLE -> {
                return timeTableManager.answerCommand(message, bot);
            }

            case TASK -> {
                return taskManager.answerCommand(message, bot);
            }

            case PROGRESS -> {
                return progressControlManager.answerCommand(message, bot);
            }

            case PROFILE -> {
                return profileManager.answerCommand(message, bot);
            }

            case SEARCH -> {
                return searchManager.answerCommand(message, bot);
            }

            default -> {
                return defaultAnswer(message);
            }
        }
    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("Неподдерживаемая команда")
                .build();
    }

}
