package ie.arch.tutorbot.entity.task;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import ie.arch.tutorbot.entity.user.Role;
import ie.arch.tutorbot.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "text_content")
    String textContent;

    @Column(name = "actual_message_id")
    Integer messageId;

    @Column(name = "actual_menu_id")
    Integer menuId;

    @Enumerated(EnumType.STRING)
    CompleteStatus completeStatus;

    @Column(name = "in_creation")
    Boolean isInCreation;

    @Column(name = "has_media")
    Boolean hasMedia;

    @Column(name = "is_finished")
    Boolean isFinished;

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "user_id"), name = "tasks_teacher_student")
    List<User> users;

    public User getStudent() {
        for (User user : users) {
            if (Role.STUDENT.equals(user.getRole())) {
                return user;
            }
        }
        
        throw new NoSuchElementException("No student for task " + id);
    }

    public User getTeacher() {
        for (User user : users) {
            if (Role.TEACHER.equals(user.getRole())) {
                return user;
            }
        }

        throw new NoSuchElementException("No teacher for task " + id);
    }

    public void changeUser(User student) {
        if (Role.TEACHER.equals(student.getRole())) {
            throw new IllegalArgumentException("Asked student, teacher given");
        }

        users.removeIf(user -> Role.STUDENT.equals(user.getRole()));
        users.add(student);
    }

}
