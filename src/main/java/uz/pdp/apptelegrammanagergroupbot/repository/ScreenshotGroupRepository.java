package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.ScreenshotGroup;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenshotGroupRepository extends JpaRepository<ScreenshotGroup, Long> {
    @Cacheable(value = "screenshotGroupEntity", key = "#id")
    @Override
    Optional<ScreenshotGroup> findById(Long id);

    @Cacheable(value = "findAll", key = "#groupId")
    List<ScreenshotGroup> findAllByGroupIdAndStatus(Long groupId, ScreenshotStatus status);

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    @CachePut(value = "screenshotGroupEntity", key = "#screenshotGroup.id")
    default Optional<ScreenshotGroup> saveOptional(ScreenshotGroup screenshotGroup) {
        return Optional.of(save(screenshotGroup));
    }

    @Cacheable(value = "screenshotGroupEntityBySender", key = "#groupId" + "+" + "#userId")
    Optional<ScreenshotGroup> findByGroupIdAndSendUserIdAndStatus(Long groupId, Long userId, ScreenshotStatus status);

    default void deleteAndClearCache(ScreenshotGroup screenshotGroup) {
        findAllCache(screenshotGroup.getGroupId());
        clearCache(screenshotGroup.getGroupId(), screenshotGroup.getSendUserId());
        clearById(screenshotGroup.getId());
        delete(screenshotGroup);
    }


    @CacheEvict(value = "screenshotGroupEntity", key = "#id")
    default void clearById(Long id) {

    }

    @CacheEvict(value = "screenshotGroupEntityBySender", key = "#groupId" + "+" + "#userId")
    default void clearCache(Long groupId, Long userId) {

    }

    @CacheEvict(value = "findAll", key = "#screenshotGroup.groupId")
    default void findAllCache(Long groupId) {
    }

}