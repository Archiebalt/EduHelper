package ie.arch.tutorbot.service.manager.help;

import static ie.arch.tutorbot.service.data.CallbackData.START;

import java.util.List;

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
public class HelpManager extends AbstractManager {

    AnswerMethodFactory answerMethodFactory;
    KeyboardFactory keyboardFactory;

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return answerMethodFactory.getSendMessage(
                message.getChatId(),

                """
                        📍 Доступные команды:
                        Нажмите на "Меню", чтобы увидеть список доступных команд

                        📍 Доступные функции:
                        - Расписание (/timetable)
                        - Домашнее задание (/task)
                        - Контроль успеваемости (/progress)
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Главное меню"),
                        List.of(1),
                        List.of(START)));
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        return answerMethodFactory.getEditMessageText(callbackQuery,
                """
                        📍 Доступные команды:
                        - start
                        - help
                        - feedback

                        📍 Доступные функции:
                        - Расписание
                        - Домашнее задание
                        - Контроль успеваемости
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("⬅️ Назад"),
                        List.of(1),
                        List.of(START)));
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

}
