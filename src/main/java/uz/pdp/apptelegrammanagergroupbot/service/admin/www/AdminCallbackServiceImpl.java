package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.entity.Tariff;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
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

    @Override

    public void process(CallbackQuery callbackQuery, Long adminId) {
        User user = adminUserState.getUser(callbackQuery.getFrom().getId());
        String data = callbackQuery.getData();
        if (data.startsWith(AdminConstants.BUY_REQUEST_DATA))
            buyRequest(callbackQuery);
        else if (data.startsWith(AdminConstants.DELETE_REQUEST_DATA))
            deleteRequest(callbackQuery, adminId);
        else if (data.startsWith(AdminConstants.TARIFF_ID_DATA))
            selectTariff(callbackQuery);
        else if (data.startsWith(AppConstant.BACK_DATA))
            refreshRequestList(callbackQuery, adminId);
        else if (data.startsWith(AdminConstants.PAYMENT_DATA))
            paymeOrClick(callbackQuery);
        else if (data.startsWith(AdminConstants.CODE_DATA))
            joinWithCode(callbackQuery);
        else if (data.startsWith(AdminConstants.SCREENSHOT_DATA))
            joinWithScreen(callbackQuery);
    }

    private void joinWithScreen(CallbackQuery callbackQuery) {

    }

    private void joinWithCode(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        adminUserState.setState(userId, StateEnum.SENDING_CODE);
        adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        adminBotSender.exe(userId, "Отправте пороля", null);
    }

    private void paymeOrClick(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        String paymeOrClick = split[0].split(":")[1];
        Long tariffId = Long.parseLong(split[1].split(":")[1]);
        Long requestId = Long.parseLong(split[2].split(":")[1]);
        Long groupId = Long.parseLong(split[3].split(":")[1]);
        adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        if (paymeOrClick.equals("payme")) {
            adminBotSender.exe(callbackQuery.getFrom().getId(), "Генерация инвойс для payme", null);
            return;
        }
        adminBotSender.exe(callbackQuery.getFrom().getId(), "Генерация инвойс для click", null);
    }

    private void selectTariff(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        Long tariffId = Long.parseLong(split[0].split(":")[1]);
        Long requestId = Long.parseLong(split[1].split(":")[1]);
        Long groupId = Long.parseLong(split[2].split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        List<Map<String, String>> list = new ArrayList<>();
        Group group = optionalGroup.get();
        if (group.isPayment()) {
            list.add(Map.of("Payme", AdminConstants.PAYMENT_DATA + "payme",
                    "Click", AdminConstants.PAYMENT_DATA + "click" + callbackQuery.getData()));
        }
        if (group.isCode()) {
            list.add(Map.of(AdminConstants.CODE_TEXT, AdminConstants.CODE_DATA + callbackQuery.getData()));
        }
        if (group.isScreenShot()) {
            list.add(Map.of(AdminConstants.SCREENSHOT_TEXT, AdminConstants.SCREENSHOT_DATA + callbackQuery.getData()));
        }
        adminButtonService.callbackKeyboard(list, 1, false);
    }

    private void deleteRequest(CallbackQuery callbackQuery, Long adminId) {
        Long requestId = Long.parseLong(callbackQuery.getData().split("\\+")[0].split(":")[1]);
        joinGroupRequestRepository.deleteById(requestId);
        refreshRequestList(callbackQuery, adminId);

    }

    private void refreshRequestList(CallbackQuery callbackQuery, Long adminId) {
        SendMessage sendMessage = adminMessageService.showRequestLists(callbackQuery.getFrom().getId(), adminId);
        if (sendMessage == null) {
            adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        if (sendMessage.getReplyMarkup() == null || ((InlineKeyboardMarkup) sendMessage.getReplyMarkup()).getKeyboard().isEmpty()) {
            adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            adminBotSender.exe(callbackQuery.getFrom().getId(), AdminConstants.HAVE_NOT_ANY_REQUESTS, new ReplyKeyboardRemove(true));
            return;
        }
        adminBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), sendMessage.getText());
        adminBotSender.changeKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), sendMessage.getReplyMarkup());
    }

    private void buyRequest(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String[] split = data.split("\\+");
        Long groupId = Long.parseLong(split[1].split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        Group group = optionalGroup.get();
        List<Tariff> tariffs = group.getTariffs();
        if (tariffs.isEmpty()) {
            adminBotSender.delete(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Все тарифы:");
        Collections.sort(tariffs);
        List<Map<String, String>> list = new ArrayList<>();
        int i = 1;
        for (Tariff tariff : tariffs) {
            sb.append(i).append(". ").append(tariff.getName());
            list.add(Map.of(i + ". " + tariff.getName(),
                    AdminConstants.TARIFF_ID_DATA + tariff.getId() + "+" + data));
        }
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));
        ReplyKeyboard replyKeyboard = adminButtonService.callbackKeyboard(list, 1, false);
        adminBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), sb.toString());
        adminBotSender.changeKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), replyKeyboard);
    }


}
