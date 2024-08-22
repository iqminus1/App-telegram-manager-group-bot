package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.CodeGroupRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.GroupRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.JoinGroupRequestRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {
    private final AdminUserState adminUserState;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final GroupRepository groupRepository;
    private final AdminBotSender adminBotSender;
    private final AdminButtonService adminButtonService;
    private final Temp temp;
    private final CodeGroupRepository codeGroupRepository;

    @Override

    public void process(Message message, Long adminId) {
        User user = adminUserState.getUser(message.getFrom().getId());
        if (message.hasText()) {
            String text = message.getText();
            if (text.equalsIgnoreCase("/start")) {
                start(message, adminId);
            } else if (user.getState().equals(StateEnum.START)) {
                if (text.equals(AdminConstants.SHOW_PRICE)) {
                    showPriceList(message, adminId);
                }
            } else if (user.getState().equals(StateEnum.SENDING_CODE)) {
                checkJoinCode(message, adminId);
            }
        } else if (message.hasPhoto()) {
            saveScreenshot(message, adminId);
        }
    }

    private void saveScreenshot(Message message, Long adminId) {
        Long groupId = temp.getTempGroupId(message.getFrom().getId());
        String fileId = message.getPhoto().get(0).getFileId();
        Long userId = message.getFrom().getId();

    }

    private void checkJoinCode(Message message, Long adminId) {
        Long groupId = temp.getTempGroupId(message.getFrom().getId());
        if (groupRepository.findByGroupId(groupId).isEmpty()) {
            adminUserState.setState(message.getFrom().getId(), StateEnum.START);
            adminBotSender.exe(message.getFrom().getId(), AdminConstants.INVALID_CODE, showRequestLists(message.getFrom().getId(), adminId).getReplyMarkup());
            return;
        }
        codeGroupRepository.findByCodeAndGroupId(message.getText(), groupId).ifPresent(code -> {
            code.setActive(true);
            code.setActiveAt(new Timestamp(System.currentTimeMillis()));
            code.setUserId(message.getFrom().getId());
            codeGroupRepository.saveOptional(code);
            joinGroupRequestRepository.findByUserIdAndGroupId(message.getFrom().getId(), groupId).ifPresent(req -> {
                adminBotSender.acceptJoinRequest(req);
                joinGroupRequestRepository.delete(req);
            });
        });
    }

    private void showPriceList(Message message, Long adminId) {
        try {
            adminBotSender.execute(showRequestLists(message.getFrom().getId(), adminId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SendMessage showRequestLists(Long chatId, Long adminId) {
        List<JoinGroupRequest> requests = getRequests(chatId, adminId);
        List<Map<String, String>> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (requests.isEmpty()) return new SendMessage();
        sb.append("Список: ").append("\n\n");
        int i = 1;
        for (JoinGroupRequest request : requests) {
            if (requests.size() != 1) {
                sb.append(i).append(". ");
            }
            sb.append(adminBotSender.getChatName(request.getGroupId())).append("\n");
            list.add(Map.of(
                    AdminConstants.TARIFF_LIST_TEXT,
                    AdminConstants.TARIFF_LIST_DATA + request.getId() + "+" + AdminConstants.GROUP_DATA + request.getGroupId(),
                    AdminConstants.DELETE_REQUEST_TEXT,
                    AdminConstants.DELETE_REQUEST_DATA + request.getId() + "+" + AdminConstants.GROUP_DATA + request.getGroupId()
            ));
        }
        ReplyKeyboard replyKeyboard = adminButtonService.callbackKeyboard(list, 1, requests.size() != 1);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(sb.toString());
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }

    private void start(Message message, Long adminId) {
        List<JoinGroupRequest> requests = getRequests(message.getFrom().getId(), adminId);
        adminUserState.setState(message.getFrom().getId(), StateEnum.START);
        if (requests.isEmpty()) {
            adminBotSender.exe(message.getFrom().getId(), AdminConstants.HAVE_NOT_ANY_REQUESTS, new ReplyKeyboardRemove(true));
            return;
        }
        adminBotSender.exe(message.getFrom().getId(), AdminConstants.START, adminButtonService.withString(List.of(AdminConstants.SHOW_PRICE), 1));
    }

    private List<JoinGroupRequest> getRequests(Long userId, Long ownerId) {
        List<Long> groupIds = groupRepository.findAllByOwnerId(ownerId).stream().map(Group::getGroupId).toList();
        List<JoinGroupRequest> joinGroupRequests = new ArrayList<>();
        for (Long groupId : groupIds) {
            joinGroupRequestRepository.findByUserIdAndGroupId(userId, groupId).ifPresent(joinGroupRequests::add);
        }
        return joinGroupRequests;
    }
}
