package ie.arch.tutorbot.entity.timetable;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "minute")
    Short minute;

}
