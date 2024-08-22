package uz.pdp.apptelegrammanagergroupbot.service.owner.scheduled;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.repository.UserRepository;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@RequiredArgsConstructor
@Component
public class SchedulingProcess {
    private final CommonUtils commonUtils;
    private final UserRepository userRepository;


}
