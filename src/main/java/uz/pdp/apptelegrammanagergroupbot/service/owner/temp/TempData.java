package uz.pdp.apptelegrammanagergroupbot.service.owner.temp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableAsync
public class TempData {

    private final Map<Long, DontUsedCodePermission> tempCode;
    private final Map<Long, UserPermission> tempPermission;

    public TempData() {
        this.tempPermission = new ConcurrentHashMap<>();
        this.tempCode = new ConcurrentHashMap<>();
    }

    @Async
    public void removeTempDataByUser(Long userId) {
        tempCode.remove(userId);
        tempPermission.remove(userId);
    }

    public void addTempCode(Long userId, DontUsedCodePermission permission) {
        tempCode.put(userId, permission);
    }

    public DontUsedCodePermission getTempCode(Long userId) {
        if (tempCode.containsKey(userId)) {
            return tempCode.get(userId);
        }
        return null;
    }

    public void addTempPermission(Long userId, UserPermission userPermission) {
        tempPermission.put(userId, userPermission);
    }

    public UserPermission getTempPermission(Long userId) {
        if (tempPermission.containsKey(userId)) {
            return tempPermission.get(userId);
        }
        return null;
    }
}
