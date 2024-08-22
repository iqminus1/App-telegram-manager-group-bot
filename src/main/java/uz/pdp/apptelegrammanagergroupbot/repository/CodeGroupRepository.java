package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CodeGroup;

import java.util.Optional;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {
    @Cacheable(value = "codeGroupEntity", key = "#code" + "+" + "#groupId")
    Optional<CodeGroup> findByCodeAndGroupId(String code, Long groupId);

    @CachePut(value = "codeGroupEntity", key = "#codeGroup.code" + "+" + "#codeGroup.groupId")
    default Optional<CodeGroup> saveOptional(CodeGroup codeGroup) {
        return Optional.of(save(codeGroup));
    }

    @CacheEvict(value = "codeGroupEntity", key = "#codeGroup.code" + "+" + "#codeGroup.groupId")
    @Override
    void delete(CodeGroup codeGroup);
}