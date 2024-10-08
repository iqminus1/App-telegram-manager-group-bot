package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.*;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

import java.util.*;

@RequiredArgsConstructor
public class AdminMessageServiceImpl implements AdminMessageService {
    private final AdminUserState adminUserState;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final GroupRepository groupRepository;
    private final AdminBotSender adminBotSender;
    private final AdminButtonService adminButtonService;
    private final Temp temp;
    private final CodeGroupRepository codeGroupRepository;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final TariffRepository tariffRepository;

    @Override

    public void process(Message message, Long adminId) {
        User user = adminUserState.getUser(message.getFrom().getId());
        if (message.getChat().getType().equals("private")) {
            if (message.hasText()) {
                String text = message.getText();
                if (text.equalsIgnoreCase("/start")) {
                    start(message, adminId);
                } else if (user.getState().equals(StateEnum.START)) {
                    if (text.equals(AdminConstants.SHOW_PRICE)) {
                        showPriceList(message, adminId);
                    } else if ((text.equals(AdminConstants.CODE_TEXT))) {
                        sendCode(message, adminId);
                    }
                } else if (user.getState().equals(StateEnum.SENDING_CODE)) {
                    checkJoinCode(message, adminId);
                }
            } else if (message.hasPhoto()) {
                saveScreenshot(message);
            }
        }
    }

    private void sendCode(Message message, Long adminId) {
        List<Group> allByOwnerId = groupRepository.findAllByOwnerId(adminId);
        if (allByOwnerId.isEmpty()) {
            return;
        }
        for (Group group : allByOwnerId) {
            Optional<JoinGroupRequest> optionalJoinGroupRequest = joinGroupRequestRepository.findByUserIdAndGroupId(message.getFrom().getId(), group.getGroupId());
            if (optionalJoinGroupRequest.isPresent()) {
                if (group.isCode()) {
                    adminUserState.setState(message.getFrom().getId(), StateEnum.SENDING_CODE);
                    adminBotSender.exe(message.getFrom().getId(), AppConstant.SEND_CODE_TEXT, null);
                }

            }
        }
    }

    private void saveScreenshot(Message message) {
        Long userId = message.getFrom().getId();
        ScreenshotGroup tempScreenshot = temp.getTempScreenshot(userId);
        if (tempScreenshot == null) {
            adminBotSender.exe(userId, AppConstant.EXCEPTION, adminButtonService.withString(List.of(AdminConstants.SHOW_PRICE, AdminConstants.CODE_TEXT), 1));
            return;
        }
        List<PhotoSize> photo = message.getPhoto();
        PhotoSize photoSize = photo.stream().max(Comparator.comparing(PhotoSize::getFileSize)).get();
        String filePath = adminBotSender.getFilePath(photoSize);
        tempScreenshot.setPath(filePath);
        Optional<ScreenshotGroup> optionalScreenshotGroup = screenshotGroupRepository.findByGroupIdAndSendUserIdAndStatus(tempScreenshot.getGroupId(), tempScreenshot.getSendUserId(), ScreenshotStatus.DONT_SEE);
        if (optionalScreenshotGroup.isEmpty()) {
            screenshotGroupRepository.saveOptional(tempScreenshot);
        } else {
            ScreenshotGroup screenshotGroup = optionalScreenshotGroup.get();
            screenshotGroup.setPath(tempScreenshot.getPath());
            screenshotGroup.setTariffId(tempScreenshot.getTariffId());
            screenshotGroupRepository.saveOptional(screenshotGroup);
        }
        adminUserState.setState(userId, StateEnum.START);
        adminBotSender.exe(userId, AdminConstants.SUCCESSFULLY_GETTING_PHOTO, null);
    }


    private void checkJoinCode(Message message, Long adminId) {
        Long userId = message.getFrom().getId();
        long tariffId = temp.getTariffId(userId);
        Tariff tariff = tariffRepository.findById(userId).get();
        Long groupId = tariff.getGroup().getGroupId();
        Optional<CodeGroup> optional = codeGroupRepository.findByCodeAndGroupId(message.getText(), groupId);
        if (optional.isEmpty()) {

            return;
        }
        codeGroupRepository.delete(optional.get());
        adminBotSender.getLink(groupId);
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
                sb.append(i++).append(". ");
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
        adminBotSender.exe(message.getFrom().getId(), AdminConstants.START, adminButtonService.withString(List.of(AdminConstants.SHOW_PRICE, AdminConstants.CODE_TEXT), 1));
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
