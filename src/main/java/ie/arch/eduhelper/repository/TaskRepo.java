package ie.arch.eduhelper.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.eduhelper.entity.task.CompleteStatus;
import ie.arch.eduhelper.entity.task.Task;
import ie.arch.eduhelper.entity.user.User;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {

    boolean existsByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    void deleteByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    int countAllByUsersContainingAndIsFinishedAndCompleteStatus(
            User user,
            Boolean isFinished,
            CompleteStatus completeStatus);

}
