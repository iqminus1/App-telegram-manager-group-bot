package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;

import java.util.Optional;

@Repository
public interface JoinGroupRequestRepository extends JpaRepository<JoinGroupRequest, Long> {
    @Cacheable(value = "joinGroupRequestEntity", key = "#userId" + "+" + "#groupId")
    Optional<JoinGroupRequest> findByUserIdAndGroupId(Long userId, Long groupId);

    @CachePut(value = "joinGroupRequestEntity", key = "#joinGroupRequest.userId" + "+" + "#joinGroupRequest.groupId")
    default Optional<JoinGroupRequest> saveOptional(JoinGroupRequest joinGroupRequest) {
        return Optional.of(save(joinGroupRequest));
    }

    @Override
    @CacheEvict(value = "joinGroupRequestEntity", key = "#joinGroupRequest.userId" + "+" + "#joinGroupRequest.groupId")
    void delete(JoinGroupRequest joinGroupRequest);

    @Override
    @CacheEvict(value = "joinGroupRequestEntity", allEntries = true)
    void deleteById(Long id);
}