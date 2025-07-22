package ie.arch.tutorbot.entity.timetable;

import java.util.List;
import java.util.UUID;

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
@Table(name = "timetable")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Timetable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    WeekDay weekDay;

    @Column(name = "hour")
    Short hour;

    @Column(name = "in_creation")
    Boolean inCreation;

    @Column(name = "minute")
    Short minute;

    @ManyToMany
    @JoinTable(
        joinColumns = @JoinColumn(name = "timetable_id"), 
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        name = "users_timetable")
    List<User> users;

}
