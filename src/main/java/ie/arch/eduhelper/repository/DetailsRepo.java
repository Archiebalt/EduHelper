package ie.arch.eduhelper.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ie.arch.eduhelper.entity.user.UserDetails;

@Repository
public interface DetailsRepo extends JpaRepository<UserDetails, UUID> {

}
