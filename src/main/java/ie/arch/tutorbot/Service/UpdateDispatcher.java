package ie.arch.tutorbot.Service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import ie.arch.tutorbot.Service.handler.CallbackQueryHandler;
import ie.arch.tutorbot.Service.handler.CommandHandler;
import ie.arch.tutorbot.Service.handler.MessageHandler;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UpdateDispatcher {

    MessageHandler messageHandler; // Объект содержащий информацию о новом сообщении в чате: текст сообщения, отправитель и т.п.

    CommandHandler commandHandler;

    CallbackQueryHandler callbackQueryHandler; // Событие, описывающий некий односложный запрос: колл бэк дата

    
    public BotApiMethod<?> distribute(Update update, Bot bot) {

        if (update.hasCallbackQuery()) {
            return callbackQueryHandler.answer(update.getCallbackQuery(), bot);
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                if (message.getText().charAt(0) == '/') {
                    return commandHandler.answer(message, bot);
                }
            }

            return messageHandler.answer(message, bot);

        }

        log.info("Unsupported update: " + update);
        return null;

    }

}
