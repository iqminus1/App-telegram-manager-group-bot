package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinGroupRequestRepository extends JpaRepository<JoinGroupRequest, Long> {
    Optional<JoinGroupRequest> findByUserIdAndGroupId(Long userId, Long groupId);
}