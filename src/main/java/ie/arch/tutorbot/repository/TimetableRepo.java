package ie.arch.tutorbot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.tutorbot.entity.timetable.Timetable;
import ie.arch.tutorbot.entity.timetable.WeekDay;
import ie.arch.tutorbot.entity.user.User;

@Repository
public interface TimetableRepo extends JpaRepository<Timetable, UUID> {

    List<Timetable> findAllByUsersContainingAndWeekDay(User user, WeekDay weekDay);

    Timetable findTimetableById(UUID id);

}
