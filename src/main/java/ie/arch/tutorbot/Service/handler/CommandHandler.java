package ie.arch.tutorbot.Service.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.Service.manager.feedback.FeedbackManager;
import ie.arch.tutorbot.Service.manager.help.HelpManager;
import ie.arch.tutorbot.Service.manager.progress_control.ProgressControlManager;
import ie.arch.tutorbot.Service.manager.start.StartManager;
import ie.arch.tutorbot.Service.manager.task.TaskManager;
import ie.arch.tutorbot.Service.manager.timetable.TimetableManager;
import ie.arch.tutorbot.telegram.Bot;

import static ie.arch.tutorbot.Service.data.Command.*;

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

            default -> {
                return defaultAnswer(message);
            }
        }

    }

    private BotApiMethod<?> start(Message message) {

        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("""

                            🖖Приветствую в Tutor-Bot, инструменте для упрощения взаимодействия репититора и ученика.

                            Что бот умеет?
                            📌 Составлять расписание
                            📌 Прикреплять домашние задания
                            📌 Ввести контроль успеваемости

                        """)

                .build();

    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage
                .builder()
                .chatId(message.getChatId())
                .text("Неподдерживаемая команда")
                .build();
    }

}
