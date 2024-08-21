package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CodeGroup;

import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<CodeGroup, Long> {
    Optional<CodeGroup> findByCodeAndGroupId(String code, Long groupId);
}