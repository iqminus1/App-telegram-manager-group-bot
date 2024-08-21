package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Temp {
    private final Map<Long, Long> userIdAndGroupId = new ConcurrentHashMap<>();

    public void removeTempDataByUser(Long userId) {
        userIdAndGroupId.remove(userId);
    }

    public void addJoinCode(Long userId, Long groupId) {
        userIdAndGroupId.put(userId, groupId);
    }

    public Long getTempGroupId(Long userId) {
        if (userIdAndGroupId.containsKey(userId)) {
            return userIdAndGroupId.get(userId);
        }
        return null;
    }
}
