package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.UserJoinGroupPermission;

import java.util.Optional;

@Repository
public interface UserJoinGroupPermissionRepository extends JpaRepository<UserJoinGroupPermission, Long> {
    @Cacheable(value = "userJoinGroupPermission", key = "#userId+#groupId")
    Optional<UserJoinGroupPermission> findByUserIdAndGroupId(Long userId, Long groupId);

    @CachePut(value = "userJoinGroupPermission", key = "#userJoinGroupPermission.userId+#userJoinGroupPermission.groupId")
    default Optional<UserJoinGroupPermission> saveOptional(UserJoinGroupPermission userJoinGroupPermission) {
        return Optional.of(save(userJoinGroupPermission));
    }
}