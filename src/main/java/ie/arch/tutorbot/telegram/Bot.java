package ie.arch.tutorbot.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Bot extends TelegramWebhookBot {

    TelegramProperties telegramProperties;

    public Bot(TelegramProperties telegramProperties) {
        super(telegramProperties.getToken());
        this.telegramProperties = telegramProperties;
    }

    @Override
    public String getBotPath() {
        return telegramProperties.getPath();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getUsername();
    }

}
