package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CreatorCode;

import java.util.Optional;

@Repository
public interface CreatorCodeRepository extends JpaRepository<CreatorCode, Long> {
    @Cacheable(value = "ownerCreatorCode",key = "#code")
    Optional<CreatorCode> findByCode(String code);

    @CachePut(value ="ownerCreatorCode",key = "#creatorCode.code")
    @Override
    CreatorCode save(CreatorCode creatorCode);

    @CacheEvict(value = "ownerCreatorCode",key = "#creatorCode.code")
    @Override
    void delete(CreatorCode creatorCode);
}