package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.pdp.apptelegrammanagergroupbot.entity.*;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.DontUsedCodePermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.GroupRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.UserPermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.service.owner.temp.TempData;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final TempData tempData;
    private final OwnerBotSender ownerBotSender;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final GroupRepository groupRepository;

    @Override
    public void process(CallbackQuery callbackQuery) {
        User user = commonUtils.getUser(callbackQuery.getFrom().getId());
        String data = callbackQuery.getData();
        if (user.getState().equals(StateEnum.CHOOSE_PERMISSION)) {
            if (data.startsWith(AppConstant.PERMISSION_CODE_FOR_DATA))
                selectPermissionType(callbackQuery);
        } else if (user.getState().equals(StateEnum.PERMISSION_EXPIRE)) {
            if (data.startsWith(AppConstant.MONTH_DATA)) {
                permissionSetMonth(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backChoosePermissionCodeType(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.CHOOSES_PAYMENT)) {
            if (data.startsWith(AppConstant.ACCEPT_PERMISSION_DATA)) {
                acceptPermissionGenerate(callbackQuery);
            } else if (data.startsWith("true:") || data.startsWith("false:")) {
                changePermissionStatus(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backChoosePermissionExpire(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.ADMIN_SELECTING_PAYMENT)) {
            if (data.startsWith(AppConstant.ACCEPT_PERMISSION_DATA)) {
                acceptAdminPermission(callbackQuery);
            } else if (data.startsWith("true:") || data.startsWith("false:")) {
                changeAdminPermissionStatus(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.SETTINGS_GROUP)) {
            if (data.startsWith(AppConstant.SHOW_GROUP_INFO)) {
                showManageGroupInfo(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backShowGroupList(callbackQuery);
            } else if (data.startsWith(AppConstant.MANAGE_GROUP_PRICE_DATA)) {
                manageGroupPrice(callbackQuery);
            } else if (data.startsWith(AppConstant.MANAGE_GROUP_PAYMENT_DATA)) {
                showGroupPaymentInfo(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.SETTINGS_PAYMENT) || user.getState().equals(StateEnum.SETTINGS_PRICE)) {
            if (callbackQuery.getData().startsWith("true:") || callbackQuery.getData().startsWith("false")) {
                changeStatus(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                showManageGroupInfo(callbackQuery);
            } else if ((data.startsWith(AppConstant.ADD_TARIFF_DATA))) {
                addTariff(callbackQuery);
            }
        }
    }

    private void addTariff(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split("\\+")[1].split(":")[1]);
        Tariff tariff = new Tariff();
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        ownerBotSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        if (optionalGroup.isEmpty()) {
            commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.START);
            ownerBotSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        tariff.setGroup(optionalGroup.get());
        Long userId = callbackQuery.getFrom().getId();
        tempData.addTempTariff(userId, tariff);
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_NAME);
        ownerBotSender.exe(userId, AppConstant.SEND_TARIFF_NAME, new ReplyKeyboardRemove(true));
    }

    private void changeStatus(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        String type = split[0].split(":")[1];
        long groupId = Long.parseLong(split[1].split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.START);
            ownerBotSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        Group group = optionalGroup.get();
        switch (type) {
            case "Payment" -> group.setPayment(!group.isPayment());
            case "Code" -> group.setCode(!group.isCode());
            case "Screenshot" -> group.setScreenShot(!group.isScreenShot());
        }
        groupRepository.saveOptional(group);
        callbackQuery.setData(AppConstant.MANAGE_GROUP_PAYMENT_DATA + groupId);
        showGroupPaymentInfo(callbackQuery);
    }

//    private void backToShowManageGroupInfo(CallbackQuery callbackQuery) {
//        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
//        String text = getShowManageGroupInfoText(groupId);
//
//    }

    private void manageGroupPrice(CallbackQuery callbackQuery) {
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.SETTINGS_PRICE);
        Long userId = callbackQuery.getFrom().getId();
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        Group group = optionalGroup.get();
        List<Map<String, String>> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ownerBotSender.getChatName(groupId)).append(": ").append("\n\n").append("-----Тарафи-----");
        int i = 1;
        List<Tariff> tariffs = group.getTariffs();
        Collections.sort(tariffs);
        for (Tariff tariff : tariffs) {
            sb.append("\n").append(i++).append(". ").append(tariff.getName()).append("\n-----------");

            Map<String, String> map = new HashMap<>();
            map.put(tariff.getName(), AppConstant.TARIFF_DATA + tariff.getId() + AppConstant.GROUP_DATA + groupId);
            list.add(map);
        }
        list.add(Map.of(AppConstant.ADD_TARIFF_TEXT, AppConstant.ADD_TARIFF_DATA + "+" + AppConstant.GROUP_DATA + groupId));
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + groupId));
        changeText(callbackQuery, sb.toString());
        changeReply(callbackQuery, (InlineKeyboardMarkup) buttonService.callbackKeyboard(list, 1, false));
    }

    private void showManageGroupInfo(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        String sb = getShowManageGroupInfoText(groupId);
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.SETTINGS_GROUP);
        ReplyKeyboard keyboard = getShowManageGroupInfoKeyboard(groupId);
        changeText(callbackQuery, sb);
        changeReply(callbackQuery, (InlineKeyboardMarkup) keyboard);
    }

    private String getShowManageGroupInfoText(long groupId) {
        StringBuilder sb = new StringBuilder();
        sb.append(ownerBotSender.getChatName(groupId)).append(": ").append("\n\n");
        sb.append(AppConstant.SELECT_CHOOSE);
        return sb.toString();
    }

    private void changeReply(CallbackQuery callbackQuery, InlineKeyboardMarkup keyboard) {
        ownerBotSender.changeKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), keyboard);
    }

    private void changeText(CallbackQuery callbackQuery, String sb) {
        ownerBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), sb.toString());
    }

    private ReplyKeyboard getShowManageGroupInfoKeyboard(long groupId) {
        List<Map<String, String>> list = List.of(
                Map.of(AppConstant.MANAGE_GROUP_PAYMENT_TEXT,
                        AppConstant.MANAGE_GROUP_PAYMENT_DATA + groupId),
                Map.of(AppConstant.MANAGE_GROUP_PRICE_TEXT,
                        AppConstant.MANAGE_GROUP_PRICE_DATA + groupId),
                Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));

        return buttonService.callbackKeyboard(list, 1, false);
    }

    private void backShowGroupList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        SendMessage groupSettings = buttonService.getGroupSettings(userId);
        if (groupSettings == null) {
            ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
        ownerBotSender.changeText(userId, callbackQuery.getMessage().getMessageId(), groupSettings.getText());
        ownerBotSender.changeKeyboard(userId, callbackQuery.getMessage().getMessageId(), (InlineKeyboardMarkup) groupSettings.getReplyMarkup());
    }

    private void showGroupPaymentInfo(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.SETTINGS_PAYMENT);
        String chatName = ownerBotSender.getChatName(groupId);
        StringBuilder sb = new StringBuilder();
        sb.append(chatName).append(": ").append("\n\n");
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.START);
            ownerBotSender.exe(callbackQuery.getFrom().getId(), AppConstant.EXCEPTION, buttonService.startButton(callbackQuery.getFrom().getId()));
            return;
        }
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(callbackQuery.getFrom().getId());
        if (optionalUserPermission.isEmpty()) {
            commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.START);
            ownerBotSender.exe(callbackQuery.getFrom().getId(), AppConstant.EXCEPTION, buttonService.startButton(callbackQuery.getFrom().getId()));
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        Group group = optionalGroup.get();
        List<Map<String, String>> list = new ArrayList<>();
        int i = 1;
        if (userPermission.isPayment()) {
            Map<String, String> map = new HashMap<>();
            sb.append(i++).append(". ");
            if (group.isPayment()) {
                sb.append(AppConstant.OFF_PAYMENT_TEXT).append(" ").append(AppConstant.TRUE);
                map.put(AppConstant.FOR_OFF + AppConstant.FALSE, "false:Payment" + "+groupId:" + groupId);
            } else {
                sb.append(AppConstant.ON_PAYMENT_TEXT).append(" ").append(AppConstant.FALSE);
                map.put(AppConstant.FOR_ON + AppConstant.TRUE, "true:Payment" + "+groupId:" + groupId);
            }
            sb.append("\n");
            list.add(map);
        }
        if (userPermission.isCode()) {
            Map<String, String> map = new HashMap<>();
            sb.append(i++).append(". ");
            if (group.isCode()) {
                sb.append(AppConstant.OFF_CODE_GENERATION).append(" ").append(AppConstant.TRUE);
                map.put(AppConstant.FOR_OFF + AppConstant.FALSE, "false:Code" + "+groupId:" + groupId);
            } else {
                sb.append(AppConstant.ON_CODE_GENERATION).append(" ").append(AppConstant.FALSE);
                map.put(AppConstant.FOR_ON + AppConstant.TRUE, "true:Code" + "+groupId:" + groupId);
            }
            sb.append("\n");
            list.add(map);
        }
        if (userPermission.isScreenshot()) {
            Map<String, String> map = new HashMap<>();
            sb.append(i).append(". ");
            if (group.isScreenShot()) {
                sb.append(AppConstant.OFF_SCREENSHOT).append(" ").append(AppConstant.TRUE);
                map.put(AppConstant.FOR_OFF + AppConstant.FALSE, "false:Screenshot" + "+groupId:" + groupId);
            } else {
                sb.append(AppConstant.ON_SCREENSHOT).append(" ").append(AppConstant.FALSE);
                map.put(AppConstant.FOR_ON + AppConstant.TRUE, "true:Screenshot" + "+groupId:" + groupId);
            }
            sb.append("\n");
            list.add(map);
        }
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + groupId));
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(list, 1, false);
        changeText(callbackQuery, sb.toString());
        changeReply(callbackQuery, (InlineKeyboardMarkup) replyKeyboard);
    }

    private void changeAdminPermissionStatus(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        int i = Integer.parseInt(callbackQuery.getData().split(":")[1]);
        UserPermission tempPermission = tempData.getTempPermission(userId);
        if (i == 1)
            tempPermission.setPayment(!tempPermission.isPayment());
        if (i == 2)
            tempPermission.setCode(!tempPermission.isCode());
        if (i == 3)
            tempPermission.setScreenshot(!tempPermission.isScreenshot());

        ownerBotSender.changeText(userId, messageId, choosePaymentString(tempPermission.isPayment(), tempPermission.isCode(), tempPermission.isScreenshot()));
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) buttonService.generateKeyboardPermissionStatus(tempPermission.isPayment(), tempPermission.isCode(), tempPermission.isScreenshot(), false));
    }

    private void acceptAdminPermission(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.exe(userId, AppConstant.GENERATE_INVOICE, buttonService.startButton(userId));
    }

    private void backChoosePermissionExpire(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.PERMISSION_EXPIRE);
        ownerBotSender.changeText(userId, messageId, AppConstant.SELECT_ANY_EXPIRE);
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) choosePermissionExpire());
    }

    private void changePermissionStatus(CallbackQuery callbackQuery) {
        int i = Integer.parseInt(callbackQuery.getData().split(":")[1]);
        DontUsedCodePermission dontUsedCodePermission = tempData.getTempCode(callbackQuery.getFrom().getId());
        if (i == 1)
            dontUsedCodePermission.setPayment(!dontUsedCodePermission.isPayment());
        if (i == 2)
            dontUsedCodePermission.setCodeGeneration(!dontUsedCodePermission.isCodeGeneration());
        if (i == 3)
            dontUsedCodePermission.setScreenshot(!dontUsedCodePermission.isScreenshot());
        ReplyKeyboard replyKeyboard = buttonService.generateKeyboardPermissionStatus(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot(), true);
        String text = choosePaymentString(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        ownerBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), text);
        changeReply(callbackQuery, (InlineKeyboardMarkup) replyKeyboard);

    }

    private void acceptPermissionGenerate(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        DontUsedCodePermission dontUsedCodePermission = tempData.getTempCode(userId);
        dontUsedCodePermission.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        dontUsedCodePermissionRepository.saveOptional(dontUsedCodePermission);
        tempData.removeTempDataByUser(userId);
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        ownerBotSender.exe(userId, AppConstant.GETTING_CODE + dontUsedCodePermission.getCode(), buttonService.startButton(userId));


    }

    private void backChoosePermissionCodeType(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.CHOOSE_PERMISSION);
        ownerBotSender.changeText(userId, messageId, AppConstant.GENERATE_CODE_FOR_PERMISSION_TEXT);
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) buttonService.permissionCodeType());
    }

    private void permissionSetMonth(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.CHOOSES_PAYMENT);
        Integer expire = Integer.parseInt(callbackQuery.getData().split(AppConstant.MONTH_DATA)[1]);
        DontUsedCodePermission dontUsedCodePermission = tempData.getTempCode(userId);
        dontUsedCodePermission.setExpireMonth(expire);
        ReplyKeyboard replyKeyboard = buttonService.generateKeyboardPermissionStatus(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot(), true);
        ownerBotSender.changeText(userId, messageId, choosePaymentString(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot()));
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) replyKeyboard);
    }


    private void selectPermissionType(CallbackQuery callbackQuery) {
        CodeType type = CodeType.valueOf(callbackQuery.getData().split(AppConstant.PERMISSION_CODE_FOR_DATA)[1]);
        Long userId = callbackQuery.getFrom().getId();
        tempData.getTempCode(userId).setType(type);


        ReplyKeyboard replyKeyboard = choosePermissionExpire();

        commonUtils.setState(userId, StateEnum.PERMISSION_EXPIRE);
        ownerBotSender.changeText(userId, callbackQuery.getMessage().getMessageId(), AppConstant.SELECT_ANY_EXPIRE);
        ownerBotSender.changeKeyboard(userId, callbackQuery.getMessage().getMessageId(), (InlineKeyboardMarkup) replyKeyboard);
    }

    private ReplyKeyboard choosePermissionExpire() {
        return buttonService.callbackKeyboard(List.of(
                Map.of(AppConstant.ONE_MONTH, AppConstant.MONTH_DATA + 1),
                Map.of(AppConstant.SIX_MONTH, AppConstant.MONTH_DATA + 6),
                Map.of(AppConstant.ONE_YEAR, AppConstant.MONTH_DATA + 12),
                Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA)), 1, false);
    }

    @Override
    public String choosePaymentString(boolean bool1, boolean bool2, boolean bool3) {
        StringBuilder sb = new StringBuilder();
        sb.append(AppConstant.WHICH_SERVICES).append("\n\n");
        sb.append("1. ").append(AppConstant.ADD_GROUP_WITH_PAYMENT);
        if (bool1)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        sb.append("\n").append("2. ").append(AppConstant.ADD_GROUP_WITH_GENERATION_CODE);
        if (bool2)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        sb.append("\n").append("3. ").append(AppConstant.ADD_GROUP_WITH_SCREENSHOT);

        if (bool3)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        return sb.toString();
    }
}
