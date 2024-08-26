package uz.pdp.apptelegrammanagergroupbot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.service.admin.www.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@EnableAsync
public class BotController {
    private final UserPermissionRepository userPermissionRepository;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final AdminButtonService adminButtonService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CodeGroupRepository codeGroupRepository;
    private final ChatMemberService chatMemberService;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final UserJoinGroupPermissionRepository userJoinGroupPermissionRepository;

    private final Map<Long, AdminBotSender> sender = new ConcurrentHashMap<>();
    private final TariffRepository tariffRepository;
//    private final Map<Long, AdminBot> bot = new ConcurrentHashMap<>();

    @Async
    public void addAdminBot(String token, String username, Long adminId) {
        AdminBotSender botSender = new AdminBotSender(token);
        Temp temp = new Temp();
        AdminUserState adminUserState = new AdminUserState(userRepository);
        AdminMessageService adminMessageService = new AdminMessageServiceImpl(adminUserState, joinGroupRequestRepository, groupRepository, botSender, adminButtonService, temp, codeGroupRepository, screenshotGroupRepository,tariffRepository);
        AdminCallbackService adminCallbackService = new AdminCallbackServiceImpl(adminUserState, groupRepository, botSender, adminButtonService, joinGroupRequestRepository, adminMessageService, temp, tariffRepository);
        JoinRequestService joinRequestService = new JoinRequestServiceImpl(botSender, joinGroupRequestRepository, adminButtonService, userJoinGroupPermissionRepository);
        new AdminBot(token, username, adminId, userPermissionRepository, botSender, joinGroupRequestRepository, joinRequestService, chatMemberService, adminMessageService, adminCallbackService, groupRepository, botSender);

        sender.put(adminId, botSender);
//        bot.put(adminId, adminBot);
    }

    public void sendMessage(Long getterUser, String text, Long adminId) {
        if (sender.containsKey(adminId)) {
            AdminBotSender botSender = sender.get(adminId);
            botSender.exe(getterUser, text);
        }
    }

}
