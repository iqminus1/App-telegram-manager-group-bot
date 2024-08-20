package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.apptelegrammanagergroupbot.entity.Creator;

import java.util.Optional;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
    Optional<Creator> findByUserId(Long userId);
}