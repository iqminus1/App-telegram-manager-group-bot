package uz.pdp.apptelegrammanagergroupbot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@EnableScheduling
@RequiredArgsConstructor
@Component
public class CommonUtils {
    public final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public User getUser(Long userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        }
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isPresent()) {
            users.put(userId, optional.get());
            return optional.get();
        }
        User user = userRepository.saveOptional(new User(userId, StateEnum.START)).get();
        users.put(userId, user);
        return user;
    }

    public User setState(Long userId, StateEnum state) {
        User user = getUser(userId);
        user.setState(state);
        return user;
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void clearUserList() {
        for (Long userId : users.keySet()) {
            User user = users.get(userId);
            userRepository.saveOptional(user);
        }
        users.clear();
    }

}
