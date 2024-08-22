package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Cacheable(value = "groupEntityByOwnerId", key = "#ownerId")
    List<Group> findAllByOwnerId(Long ownerId);

    @Cacheable(value = "groupEntityGroupId", key = "#groupId")
    Optional<Group> findByGroupId(Long groupId);

    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId"}, allEntries = true)
    default Optional<Group> saveOptional(Group group) {
        return Optional.of(save(group));
    }

    @Override
    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId"}, allEntries = true)
    void delete(Group group);
}