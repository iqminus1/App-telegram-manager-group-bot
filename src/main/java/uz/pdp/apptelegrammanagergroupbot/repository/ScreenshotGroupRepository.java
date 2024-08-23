package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.ScreenshotGroup;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;

import java.util.List;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {
    @Cacheable(value = "findAll", key = "#groupId")
    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, ScreenshotStatus status);

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    ScreenshotGroup save(ScreenshotGroup screenshotGroup);

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    void delete(ScreenshotGroup screenshotGroup);
}