package ie.arch.tutorbot.Service.manager.progress_control;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.Service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressControlManager extends AbstractManager {

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'answerCommand'");
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'answerMessage'");
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'answerCallbackQuery'");
    }

}
