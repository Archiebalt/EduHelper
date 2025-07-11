package ie.arch.tutorbot.Service.handler;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.telegram.Bot;

@Service
public class CommandHandler {

    public BotApiMethod<?> answer(Message message, Bot bot) {
        return null;
    }

}
