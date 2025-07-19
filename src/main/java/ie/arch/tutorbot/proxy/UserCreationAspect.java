package ie.arch.tutorbot.proxy;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import ie.arch.tutorbot.entity.user.Action;
import ie.arch.tutorbot.entity.user.Role;
import ie.arch.tutorbot.entity.user.UserDetails;
import ie.arch.tutorbot.repository.DetailsRepo;
import ie.arch.tutorbot.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

@Aspect
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserCreationAspect {

    UserRepo userRepo;

    DetailsRepo detailsRepo;

    @Pointcut("execution(* ie.arch.tutorbot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {

    }

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];
        User telegramUser;

        if (update.hasMessage()) {
            telegramUser = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            telegramUser = update.getCallbackQuery().getFrom();
        } else {
            return joinPoint.proceed();
        }

        if (userRepo.existsById(telegramUser.getId())) {
            return joinPoint.proceed();
        }

        // Информация о пользователе
        UserDetails details = UserDetails.builder()
                .firstname(telegramUser.getFirstName())
                .username(telegramUser.getUserName())
                .lastname(telegramUser.getLastName())
                .registeredAt(LocalDateTime.now())
                .build();

        detailsRepo.save(details);

        ie.arch.tutorbot.entity.user.User newUser = ie.arch.tutorbot.entity.user.User.builder()

                .chatId(telegramUser.getId())
                .action(Action.FREE)
                .role(Role.EMPTY)
                .details(details)
                .build();

        userRepo.save(newUser);

        return joinPoint.proceed();

    }

}
