package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.apptelegrammanagergroupbot.entity.Creator;

import java.util.Optional;

public interface CreatorRepository extends JpaRepository<Creator, Long> {
    @Cacheable(value = "codeGroupEntity", key = "#userId")
    Optional<Creator> findByUserId(Long userId);

    @CachePut(value = "codeGroupEntity", key = "#creator.userId")
    default Optional<Creator> saveOptional(Creator creator) {
        return Optional.of(save(creator));
    }

    @CacheEvict(value = "codeGroupEntity", key = "#creator.userId")
    void delete(Creator creator);
}