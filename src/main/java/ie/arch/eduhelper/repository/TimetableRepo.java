package ie.arch.eduhelper.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ie.arch.eduhelper.entity.timetable.Timetable;
import ie.arch.eduhelper.entity.timetable.WeekDay;
import ie.arch.eduhelper.entity.user.User;

@Repository
public interface TimetableRepo extends JpaRepository<Timetable, UUID> {

    List<Timetable> findAllByUsersContainingAndWeekDay(User user, WeekDay weekDay);

    Timetable findTimetableById(UUID id);

    @Query("SELECT t FROM Timetable t WHERE CAST(t.id AS string) LIKE :shortId%")
    List<Timetable> findByIdStartingWith(@Param("shortId") String shortId);

}
