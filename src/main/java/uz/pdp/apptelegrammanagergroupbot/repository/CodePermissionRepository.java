package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CodePermission;

import java.util.Optional;

@Repository
public interface CodePermissionRepository extends JpaRepository<CodePermission, Long> {
    @Cacheable(value = "codePermissionEntity", key = "#id")
    @Override
    Optional<CodePermission> findById(Long id);

    @CachePut(value = "codePermissionEntity", key = "#codePermission.id")
    default Optional<CodePermission> saveOptional(CodePermission codePermission) {
        return Optional.of(save(codePermission));
    }

    @CacheEvict(value = "codePermissionEntity",key = "#codePermission.id")
    @Override
    void delete(CodePermission codePermission);
}