package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.Tariff;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @CacheEvict(value = {"groupEntityGroupId", "groupEntityByOwnerId"}, allEntries = true)
    @Override
    Tariff save(Tariff tariff);
}