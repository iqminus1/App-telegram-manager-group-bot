package uz.pdp.apptelegrammanagergroupbot.service.owner.temp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableAsync
public class TempData {

    private final Map<Long, DontUsedCodePermission> tempCode;

    public TempData() {
        this.tempCode = new ConcurrentHashMap<>();
    }

    @Async
    public void deleteTempIfAdmin(Long userId) {
        tempCode.remove(userId);
    }

    public void addTempCode(Long userId, DontUsedCodePermission permission) {
        tempCode.put(userId, permission);
    }

    public DontUsedCodePermission get(Long userId) {
        if (tempCode.containsKey(userId)) {
            return tempCode.get(userId);
        }
        return null;
    }
}
