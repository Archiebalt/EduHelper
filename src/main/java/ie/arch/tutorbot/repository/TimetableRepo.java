package ie.arch.tutorbot.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.tutorbot.entity.timetable.Timetable;

@Repository
public interface TimetableRepo extends JpaRepository<Timetable, UUID> {

}
