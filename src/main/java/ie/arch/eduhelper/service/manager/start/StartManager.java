package ie.arch.eduhelper.service.manager.start;

import static ie.arch.eduhelper.service.data.CallbackData.*;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import ie.arch.eduhelper.service.factory.AnswerMethodFactory;
import ie.arch.eduhelper.service.factory.KeyboardFactory;
import ie.arch.eduhelper.service.manager.AbstractManager;
import ie.arch.eduhelper.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StartManager extends AbstractManager {

    AnswerMethodFactory answerMethodFactory;
    KeyboardFactory keyboardFactory;

    @Override
    public SendMessage answerCommand(Message message, Bot bot) {

        return answerMethodFactory.getSendMessage(
                message.getChatId(),

                """
                        🖖Приветствую в EduHelpep, инструменте для упрощения взаимодействия репититора и ученика.

                        Для того, чтобы начать взаимодействовать с учеником/учителем, нужно отправить его токен - команда '/search'.
                        Узнать свой токен можно по команде '/profile'

                        Нажми кнопку "Меню", чтобы увидеть больше команд
                                """,

                keyboardFactory.getInlineKeyboard(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP, FEEDBACK)));

    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,

                """
                        🖖Приветствую в EduHelpep, инструменте для упрощения взаимодействия репититора и ученика.

                        Для того, чтобы начать взаимодействовать с учеником/учителем, нужно отправить его токен - команда '/search'.
                        Узнать свой токен можно по команде '/profile'.

                        Нажми кнопку "Меню", чтобы увидеть какие команды тебе доступны
                                """,

                keyboardFactory.getInlineKeyboard(
                        List.of("Помощь", "Обратная связь"),
                        List.of(2),
                        List.of(HELP, FEEDBACK)));
    }

}
