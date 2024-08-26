package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.ScreenshotGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Temp {
    private final Map<Long, ScreenshotGroup> screenshotGroupMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> tempTariffId = new ConcurrentHashMap<>();

    public void removeTempDataByUser(Long userId) {
        screenshotGroupMap.remove(userId);
        tempTariffId.remove(userId);
    }

    public void addScreenshotGroup(Long userId, ScreenshotGroup screenshotGroup) {
        screenshotGroupMap.put(userId, screenshotGroup);
    }

    public ScreenshotGroup getTempScreenshot(Long userId) {
        if (screenshotGroupMap.containsKey(userId)) {
            return screenshotGroupMap.get(userId);
        }
        return null;
    }

    public void addTariffId(Long userId, long tariffId) {
        tempTariffId.put(userId, tariffId);
    }

    public long getTariffId(Long userId) {
        if (tempTariffId.containsKey(userId)) {
            return tempTariffId.get(userId);
        }
        return 0;
    }
}
