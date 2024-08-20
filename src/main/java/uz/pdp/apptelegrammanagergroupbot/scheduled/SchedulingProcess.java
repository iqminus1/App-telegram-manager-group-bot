package uz.pdp.apptelegrammanagergroupbot.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.repository.UserRepository;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.util.concurrent.TimeUnit;

@EnableScheduling
@RequiredArgsConstructor
@Component
public class SchedulingProcess {
    private final CommonUtils commonUtils;
    private final UserRepository userRepository;

    @Scheduled(fixedDelay = 1000,timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = "ownerBotUser", allEntries = true)
    public void clearUserList() {
        userRepository.saveAll(commonUtils.users.values());
        commonUtils.users.clear();
        ;
    }
}
