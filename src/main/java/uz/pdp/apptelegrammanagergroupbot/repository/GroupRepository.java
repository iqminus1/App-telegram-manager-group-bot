package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findAllByOwnerId(Long ownerId);
}