package ie.arch.tutorbot.service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.repository.UserRepo;
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
public class MessageHandler {

    SearchManager searchManager;

    UserRepo userRepo;

    TimetableManager timetableManager;

    TaskManager taskManager;

    public BotApiMethod<?> answer(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());

        switch (user.getAction()) {
            case SENDING_TOKEN -> {
                return searchManager.answerMessage(message, bot);
            }

            case SENDING_TITLE, SENDING_DESCRIPTION -> {
                return timetableManager.answerMessage(message, bot);
            }

            case SENDING_TASK, SENDING_MEDIA, SENDING_TEXT -> {
                return taskManager.answerMessage(message, bot);
            }

            default -> {
                return null;
            }
        }

    }

}
