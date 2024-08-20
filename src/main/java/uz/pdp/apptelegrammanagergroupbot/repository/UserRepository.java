package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}