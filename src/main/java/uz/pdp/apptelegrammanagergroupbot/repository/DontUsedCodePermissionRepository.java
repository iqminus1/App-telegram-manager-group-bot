package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;

import java.util.List;
import java.util.Optional;

@Repository
public interface DontUsedCodePermissionRepository extends JpaRepository<DontUsedCodePermission, Long> {
    @Cacheable(value = "dontUsedCodePermissionAllEntities")
    @Override
    List<DontUsedCodePermission> findAll();

    @Cacheable(value = "dontUsedCodePermissionAllEntitiesByCode")
    List<DontUsedCodePermission> findAllByCode(String text);

    @CacheEvict(value = {"dontUsedCodePermissionAllEntities", "dontUsedCodePermissionAllEntitiesByCode"}, allEntries = true)
    default Optional<DontUsedCodePermission> saveOptional(DontUsedCodePermission dontUsedCodePermission) {
        return Optional.of(save(dontUsedCodePermission));
    }

    @Override
    @CacheEvict(value = {"dontUsedCodePermissionAllEntities", "dontUsedCodePermissionAllEntitiesByCode"}, allEntries = true)
    void delete(DontUsedCodePermission dontUsedCodePermission);
}