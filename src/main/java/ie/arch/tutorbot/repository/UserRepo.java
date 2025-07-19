package ie.arch.tutorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.tutorbot.entity.user.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

}
