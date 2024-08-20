package uz.pdp.apptelegrammanagergroupbot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Component
public class CommonUtils {
    public final ConcurrentMap<Long, User> users = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    @Cacheable(value = "ownerBotUser", key = "#userId")
    public User getUser(Long userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        }
        Optional<User> optional = userRepository.findById(userId);
        if (optional.isPresent()) {
            users.put(userId, optional.get());
            return optional.get();
        }
        User user = userRepository.save(new User(userId, StateEnum.START));
        users.put(userId, user);
        return user;
    }

    @CachePut(value = "ownerBotUser", key = "#userId")
    public User setState(Long userId, StateEnum state) {
        User user = getUser(userId);
        user.setState(state);
        return user;
    }

}
