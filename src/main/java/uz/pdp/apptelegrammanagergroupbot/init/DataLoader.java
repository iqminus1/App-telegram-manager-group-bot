package uz.pdp.apptelegrammanagergroupbot.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;
import uz.pdp.apptelegrammanagergroupbot.repository.UserPermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.service.admin.BotController;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserPermissionRepository userPermissionRepository;
    private final BotController botController;

    @Override
    public void run(String... args) throws Exception {
        addAllBots();
    }

    @Async
    public void addAllBots() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<UserPermission> all = userPermissionRepository.findAll();
        all.forEach(userPermission -> {
            if (checkString(userPermission.getBotToken()) || checkString(userPermission.getBotUsername())) {
                botController.addAdminBot(userPermission.getBotToken(), userPermission.getBotUsername(), userPermission.getUserId());
            }
        });
    }

    private boolean checkString(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
}
