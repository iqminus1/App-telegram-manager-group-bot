package uz.pdp.apptelegrammanagergroupbot.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.service.admin.www.*;

@Component
@RequiredArgsConstructor
public class BotController {
    private final UserPermissionRepository userPermissionRepository;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final AdminButtonService adminButtonService;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CodeGroupRepository codeGroupRepository;
    private final ChatMemberService chatMemberService;
    private final ScreenshotGroupRepository screenshotGroupRepository;

    public void addAdminBot(String token, String username, Long userId) {
        AdminBotSender botSender = new AdminBotSender(token);
        Temp temp = new Temp();
        AdminUserState adminUserState = new AdminUserState(userRepository);
        AdminMessageService adminMessageService = new AdminMessageServiceImpl(adminUserState, joinGroupRequestRepository, groupRepository, botSender, adminButtonService, temp, codeGroupRepository, screenshotGroupRepository);
        AdminCallbackService adminCallbackService = new AdminCallbackServiceImpl(adminUserState, groupRepository, botSender, adminButtonService, joinGroupRequestRepository, adminMessageService, temp);
        JoinRequestService joinRequestService = new JoinRequestServiceImpl(botSender, joinGroupRequestRepository, adminButtonService, adminButtonService, adminUserState);
        new AdminBot(token, username, userId, userPermissionRepository, botSender, joinGroupRequestRepository, joinRequestService, chatMemberService, adminMessageService, adminCallbackService, groupRepository, botSender);
    }
}
