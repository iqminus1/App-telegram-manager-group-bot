package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.Tariff;

import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    @Cacheable(value = "tariffEntity", key = "#id")
    @Override
    Optional<Tariff> findById(Long id);

    @CachePut(value = "tariffEntity", key = "#tariff.id")
    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId"}, allEntries = true)
    default Optional<Tariff> saveOptional(Tariff tariff) {
        return Optional.of(save(tariff));
    }

    @Override
    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId", "tariffEntity"}, allEntries = true)
    void delete(Tariff tariff);
}