package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;

import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    @Cacheable(value = "userPermissionEntity", key = "#userId")
    Optional<UserPermission> findByUserId(Long userId);

    @CachePut(value = "userPermissionEntity", key = "#userPermission.userId")
    default Optional<UserPermission> saveOptional(UserPermission userPermission) {
        return Optional.of(save(userPermission));
    }

    @CacheEvict(value = "userPermissionEntity",key = "#userPermission.userId")
    void delete(UserPermission userPermission);

}