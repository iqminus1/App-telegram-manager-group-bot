package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;
import uz.pdp.apptelegrammanagergroupbot.entity.UserJoinGroupPermission;
import uz.pdp.apptelegrammanagergroupbot.repository.JoinGroupRequestRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.UserJoinGroupPermissionRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class JoinRequestServiceImpl implements JoinRequestService {
    private final AdminBotSender botSender;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final AdminButtonService adminButtonService;
    private final UserJoinGroupPermissionRepository userJoinGroupPermissionRepository;

    public JoinRequestServiceImpl(AdminBotSender botSender,
                                  JoinGroupRequestRepository joinGroupRequestRepository, AdminButtonService adminButtonService, UserJoinGroupPermissionRepository userJoinGroupPermissionRepository) {
        this.botSender = botSender;
        this.joinGroupRequestRepository = joinGroupRequestRepository;
        this.adminButtonService = adminButtonService;
        this.userJoinGroupPermissionRepository = userJoinGroupPermissionRepository;
    }

    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long userId = chatJoinRequest.getUser().getId();
        Long groupId = chatJoinRequest.getChat().getId();
        Optional<UserJoinGroupPermission> optional = userJoinGroupPermissionRepository.findByUserIdAndGroupId(userId, groupId);
        if (optional.isPresent()) {
            UserJoinGroupPermission userJoinGroupPermission = optional.get();
            if (userJoinGroupPermission.getExpireAt().after(new Date())) {
                botSender.acceptJoinRequest(new JoinGroupRequest(userId, groupId));
                return;
            }
        }
        botSender.openChat(userId,groupId);
        botSender.exe(userId,AdminConstants.FOR_JOIN,adminButtonService.withString(List.of(AdminConstants.SHOW_PRICE), 1));
        if (joinGroupRequestRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            joinGroupRequestRepository.saveOptional(new JoinGroupRequest(userId, groupId));
        }
    }
}
