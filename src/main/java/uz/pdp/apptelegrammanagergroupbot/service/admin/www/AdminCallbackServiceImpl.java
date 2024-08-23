package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.entity.ScreenshotGroup;
import uz.pdp.apptelegrammanagergroupbot.entity.Tariff;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.GroupRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.JoinGroupRequestRepository;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

import java.util.*;

@RequiredArgsConstructor
public class AdminCallbackServiceImpl implements AdminCallbackService {
    private final AdminUserState adminUserState;
    private final GroupRepository groupRepository;
    private final AdminBotSender adminBotSender;
    private final AdminButtonService adminButtonService;
    private final JoinGroupRequestRepository joinGroupRequestRepository;
    private final AdminMessageService adminMessageService;
    private final Temp temp;

    @Override

    public void process(CallbackQuery callbackQuery, Long adminId) {
        User user = adminUserState.getUser(callbackQuery.getFrom().getId());
        String data = callbackQuery.getData();
        if (data.startsWith(AdminConstants.TARIFF_LIST_DATA))
            tariffList(callbackQuery);
        else if (data.startsWith(AdminConstants.DELETE_REQUEST_DATA))
            deleteRequest(callbackQuery, adminId);
        else if (data.startsWith(AdminConstants.TARIFF_ID_DATA))
            selectTariff(callbackQuery);
        else if (data.startsWith(AppConstant.BACK_DATA) && user.getState().equals(StateEnum.START))
            refreshRequestList(callbackQuery, adminId);
        else if (data.startsWith(AdminConstants.PAYMENT_DATA))
            paymeOrClick(callbackQuery);
        else if (data.startsWith(AdminConstants.SCREENSHOT_DATA))
            joinWithScreen(callbackQuery);
        else if (data.startsWith(AppConstant.BACK_DATA) && user.getState().equals(StateEnum.SELECT_TARIFF)) {
            backToTariffList(callbackQuery, adminId);
        }
    }

    private void backToTariffList(CallbackQuery callbackQuery, Long adminId) {
        String data = callbackQuery.getData();
        adminUserState.setState(callbackQuery.getFrom().getId(), StateEnum.START);
        callbackQuery.setData(data.substring(data.indexOf("+") + 1));
        process(callbackQuery, adminId);
    }

    private void joinWithScreen(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        long tariffId = Long.parseLong(split[1].split(":")[1]);
        long requestId = Long.parseLong(split[2].split(":")[1]);
        long groupId = Long.parseLong(split[3].split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        adminBotSender.delete(userId, callbackQuery.getMessage().getMessageId());
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            adminBotSender.exe(userId, AppConstant.EXCEPTION, null);
            return;
        }
        Group group = optionalGroup.get();
        if (!checkString(group.getCardNumber())) {
            adminBotSender.exe(userId, AppConstant.EXCEPTION, null);
            return;
        }
        adminBotSender.exe(userId, AdminConstants.SEND_PHOTO.formatted(group.getCardNumber()), new ReplyKeyboardRemove(true));
        ScreenshotGroup screenshotGroup = new ScreenshotGroup(groupId, userId, tariffId, null, ScreenshotStatus.DONT_SEE, false, null);
        temp.addScreenshotGroup(userId, screenshotGroup);
    }

    private boolean checkString(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }


    private void paymeOrClick(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        String paymeOrClick = split[0].split(":")[1];
        Long userId = callbackQuery.getFrom().getId();
        adminBotSender.delete(userId, callbackQuery.getMessage().getMessageId());
        if (paymeOrClick.equals("payme")) {
            adminBotSender.exe(userId, "Генерация инвойс для payme", null);
            return;
        }
        adminBotSender.exe(userId, "Генерация инвойс для click", null);
    }

    private void selectTariff(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        Long groupId = Long.parseLong(split[2].split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (optionalGroup.isEmpty()) {
            adminBotSender.delete(userId, messageId);
            return;
        }
        List<Map<String, String>> list = new ArrayList<>();
        Group group = optionalGroup.get();
        if (group.isPayment()) {
            list.add(Map.of("Payme", AdminConstants.PAYMENT_DATA + "payme"));
            list.add(Map.of("Click", AdminConstants.PAYMENT_DATA + "click" + callbackQuery.getData()));
        }
        if (group.isScreenShot()) {
            list.add(Map.of(AdminConstants.SCREENSHOT_TEXT, AdminConstants.SCREENSHOT_DATA + "wow" + "+" + callbackQuery.getData()));
        }
        if (list.isEmpty()) {
            adminBotSender.delete(userId, messageId);
            adminUserState.setState(userId, StateEnum.START);
            adminBotSender.exe(userId, AppConstant.EXCEPTION, null);
            return;
        }
        adminUserState.setState(userId, StateEnum.SELECT_TARIFF);
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + "toTariffs" + callbackQuery.getData()));
        ReplyKeyboard replyKeyboard = adminButtonService.callbackKeyboard(list, 1, false);
        adminBotSender.changeText(userId, messageId, AppConstant.SELECT_CHOOSE);
        adminBotSender.changeKeyboard(userId, messageId, replyKeyboard);
    }

    private void deleteRequest(CallbackQuery callbackQuery, Long adminId) {
        Long requestId = Long.parseLong(callbackQuery.getData().split("\\+")[0].split(":")[1]);
        joinGroupRequestRepository.deleteById(requestId);
        refreshRequestList(callbackQuery, adminId);

    }

    private void refreshRequestList(CallbackQuery callbackQuery, Long adminId) {
        Long userId = callbackQuery.getFrom().getId();
        SendMessage sendMessage = adminMessageService.showRequestLists(userId, adminId);
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (sendMessage == null) {
            adminBotSender.delete(userId, messageId);
            return;
        }
        if (sendMessage.getReplyMarkup() == null || ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().isEmpty()) {
            adminBotSender.delete(userId, messageId);
            adminBotSender.exe(userId, AdminConstants.HAVE_NOT_ANY_REQUESTS, new ReplyKeyboardRemove(true));
            return;
        }
        adminBotSender.changeText(userId, messageId, sendMessage.getText());
        adminBotSender.changeKeyboard(userId, messageId, sendMessage.getReplyMarkup());
    }

    private void tariffList(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String[] split = data.split("\\+");
        Long groupId = Long.parseLong(split[1].split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (optionalGroup.isEmpty()) {
            adminBotSender.delete(userId, messageId);
            return;
        }
        Group group = optionalGroup.get();
        List<Tariff> tariffs = group.getTariffs();
        if (tariffs.isEmpty()) {
            adminBotSender.delete(userId, messageId);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Все тарифы:").append("\n\n");
        Collections.sort(tariffs);
        List<Map<String, String>> list = new ArrayList<>();
        int i = 1;
        for (Tariff tariff : tariffs) {
            sb.append(i).append(". ").append(tariff.getName()).append("\n\n");
            list.add(Map.of(tariff.getName(),
                    AdminConstants.TARIFF_ID_DATA + tariff.getId() + "+" + data));
            i++;
        }
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));
        ReplyKeyboard replyKeyboard = adminButtonService.callbackKeyboard(list, 1, false);
        adminBotSender.changeText(userId, messageId, sb.toString());
        adminBotSender.changeKeyboard(userId, messageId, replyKeyboard);
    }


}
