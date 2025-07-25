package ie.arch.eduhelper.proxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import ie.arch.eduhelper.entity.user.Action;
import ie.arch.eduhelper.entity.user.Role;
import ie.arch.eduhelper.entity.user.User;
import ie.arch.eduhelper.repository.UserRepo;
import ie.arch.eduhelper.service.manager.auth.AuthManager;
import ie.arch.eduhelper.telegram.Bot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Aspect
@Order(100)
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthAspect {

    UserRepo userRepo;

    AuthManager authManager;

    @Pointcut("execution(* ie.arch.tutorbot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut() {

    }

    @Around("distributeMethodPointcut()")
    public Object authMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Update update = (Update) joinPoint.getArgs()[0];
        User user;

        if (update.hasMessage()) {
            user = userRepo.findById(update.getMessage().getChatId()).orElseThrow();
        } else if (update.hasCallbackQuery()) {
            user = userRepo.findById(update.getCallbackQuery().getMessage().getChatId()).orElseThrow();
        } else {
            return joinPoint.proceed();
        }

        if (user.getRole() != Role.EMPTY || user.getAction() == Action.AUTH) {
            return joinPoint.proceed();
        }

        return authManager.answerMessage(update.getMessage(), (Bot) joinPoint.getArgs()[1]);

    }

}
