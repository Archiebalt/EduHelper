package ie.arch.tutorbot.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.tutorbot.entity.task.Task;
import ie.arch.tutorbot.entity.user.User;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {

    boolean existsByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

    void deleteByUsersContainingAndIsInCreation(User user, Boolean isInCreation);

}
