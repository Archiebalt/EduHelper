package ie.arch.tutorbot.service.manager.feedback;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.tutorbot.service.factory.AnswerMethodFactory;
import ie.arch.tutorbot.service.factory.KeyboardFactory;
import ie.arch.tutorbot.service.manager.AbstractManager;
import ie.arch.tutorbot.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeedbackManager extends AbstractManager {

        AnswerMethodFactory answerMethodFactory;
        KeyboardFactory keyboardFactory;

        @Override
        public BotApiMethod<?> answerCommand(Message message, Bot bot) {
                return answerMethodFactory.getSendMessage(
                                message.getChatId(),

                                """
                                                📍 Ссылки для обратной связи
                                                GitHub - https://github.com/Archiebalt
                                                Telegram - https://t.me/Archie1810
                                                """,
                                null);
        }

        @Override
        public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
                return answerMethodFactory.getEditMessageText(
                                callbackQuery,

                                """
                                                📍 Ссылки для обратной связи
                                                GitHub - https://github.com/Archiebalt
                                                Telegram - https://t.me/Archie1810
                                                """,
                                null);
        }

        @Override
        public BotApiMethod<?> answerMessage(Message message, Bot bot) {
                return null;
        }

}
