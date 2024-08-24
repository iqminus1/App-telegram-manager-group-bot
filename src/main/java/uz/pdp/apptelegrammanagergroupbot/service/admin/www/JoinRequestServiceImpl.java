package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;
import uz.pdp.apptelegrammanagergroupbot.repository.JoinGroupRequestRepository;

import java.util.List;

public class JoinRequestServiceImpl implements JoinRequestService {
    private final AdminBotSender botSender;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final AdminButtonService adminButtonService;
    private final AdminUserState adminUserState;

    public JoinRequestServiceImpl(AdminBotSender botSender,
                                  JoinGroupRequestRepository joinGroupRequestRepository, AdminButtonService buttonService, AdminButtonService adminButtonService, AdminUserState adminUserState) {
        this.botSender = botSender;
        this.joinGroupRequestRepository = joinGroupRequestRepository;
        this.adminButtonService = adminButtonService;
        this.adminUserState = adminUserState;
    }

    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long userId = chatJoinRequest.getUser().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setText(AdminConstants.FOR_JOIN);
        ReplyKeyboard replyKeyboard = adminButtonService.withString(List.of(AdminConstants.SHOW_PRICE, AdminConstants.CODE_TEXT), 1);
        sendMessage.setReplyMarkup(replyKeyboard);
        Long groupId = chatJoinRequest.getChat().getId();
        try {
            botSender.execute(sendMessage);
            if (joinGroupRequestRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
                joinGroupRequestRepository.saveOptional(new JoinGroupRequest(userId, groupId));
            }
        } catch (TelegramApiException e) {
            botSender.acceptJoinRequest(userId, groupId);
            botSender.revokeJoinRequest(userId, groupId);
            sendMessage.setText(AdminConstants.CLICK_JOIN_REQ + chatJoinRequest.getInviteLink());
        }
    }
}
