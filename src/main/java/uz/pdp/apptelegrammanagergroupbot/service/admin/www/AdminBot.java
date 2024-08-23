package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.repository.GroupRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.JoinGroupRequestRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.UserPermissionRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableScheduling
public class AdminBot extends TelegramLongPollingBot {
    public final String token;
    public final String username;
    public final Long adminId;
    private final UserPermissionRepository userPermissionRepository;
    private final JoinRequestService joinRequestService;
    private final ChatMemberService chatMemberService;
    private final AdminMessageService adminMessageService;
    private final AdminCallbackService adminCallbackService;
    private final GroupRepository groupRepository;
    private final AdminBotSender adminBotSender;

    public AdminBot(String token, String username, Long userId, UserPermissionRepository userPermissionRepository, AdminBotSender botSender, JoinGroupRequestRepository joinGroupRequestRepository, JoinRequestService joinRequestService, ChatMemberService chatMemberService, AdminMessageService adminMessageService, AdminCallbackService adminCallbackService, GroupRepository groupRepository, AdminBotSender adminBotSender) {
        super(new DefaultBotOptions(), token);
        this.token = token;
        this.username = username;
        this.adminId = userId;
        this.userPermissionRepository = userPermissionRepository;
        this.joinRequestService = joinRequestService;
        this.chatMemberService = chatMemberService;
        this.adminMessageService = adminMessageService;
        this.adminCallbackService = adminCallbackService;
        this.groupRepository = groupRepository;
        this.adminBotSender = adminBotSender;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        userPermissionRepository.findByUserId(adminId).ifPresent(userPermission -> {
            if (userPermission.getExpireDate().after(new Date())) {
                if (update.hasMessage()) {
                    adminMessageService.process(update.getMessage(), adminId);
                } else if (update.hasChatJoinRequest()) {
                    joinRequestService.process(update.getChatJoinRequest());
                } else if (update.hasMyChatMember()) {
                    chatMemberService.process(update.getMyChatMember(), username, userPermission);
                } else if (update.hasCallbackQuery())
                    adminCallbackService.process(update.getCallbackQuery(), userPermission.getUserId());
            }
        });
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Scheduled(fixedDelay = 90, timeUnit = TimeUnit.MINUTES)
    private void setGroupName() {
        List<Group> allByOwnerId = groupRepository.findAllByOwnerId(adminId);
        for (Group group : allByOwnerId) {
            String chatName = adminBotSender.getChatName(group.getGroupId());
            if (chatName != null) {
                group.setName(chatName);
                groupRepository.save(group);
            }
        }
    }
}
