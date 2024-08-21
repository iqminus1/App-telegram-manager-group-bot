package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.UserRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class AdminUserState {
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
        User user = userRepository.save(new User(userId, StateEnum.START));
        users.put(userId, user);
        return user;
    }

    public User setState(Long userId, StateEnum state) {
        User user = getUser(userId);
        user.setState(state);
        return user;
    }
}
