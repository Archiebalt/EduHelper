package ie.arch.eduhelper.proxy;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import ie.arch.eduhelper.entity.user.Action;
import ie.arch.eduhelper.entity.user.Role;
import ie.arch.eduhelper.entity.user.UserDetails;
import ie.arch.eduhelper.repository.DetailsRepo;
import ie.arch.eduhelper.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(10)
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserCreationAspect {

    UserRepo userRepo;

    DetailsRepo detailsRepo;

    @Pointcut("execution(* ie.arch.eduhelper.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {

    }

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
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

        ie.arch.eduhelper.entity.user.User newUser = ie.arch.eduhelper.entity.user.User.builder()

                .chatId(telegramUser.getId())
                .action(Action.FREE)
                .role(Role.EMPTY)
                .details(details)
                .build();

        userRepo.save(newUser);

        return joinPoint.proceed();
    }

}
