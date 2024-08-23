package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.ScreenshotGroup;

import java.util.List;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {
    @Cacheable(value = "findAll")
    List<ScreenshotGroup> findAll();

    @CacheEvict(value = "findAll",allEntries = true)
    ScreenshotGroup save(ScreenshotGroup screenshotGroup);

    @CacheEvict(value = "findAll",allEntries = true)
    void delete(ScreenshotGroup screenshotGroup);
}