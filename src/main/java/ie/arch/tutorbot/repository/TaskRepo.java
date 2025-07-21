package ie.arch.tutorbot.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.tutorbot.entity.task.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {

}
