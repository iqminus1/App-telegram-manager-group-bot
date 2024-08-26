package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.*;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.service.owner.temp.TempData;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

@Service
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final TempData tempData;
    private final OwnerBotSender ownerBotSender;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final GroupRepository groupRepository;
    private final MessageService messageService;
    private final CodeService codeService;
    private final TariffRepository tariffRepository;
    private final CodeGroupRepository codeGroupRepository;
    private final ScreenshotGroupRepository screenshotGroupRepository;
    private final JoinGroupRequestRepository joinGroupRequestRepository;

    public CallbackServiceImpl(CommonUtils commonUtils, ButtonService buttonService, TempData tempData, OwnerBotSender ownerBotSender, DontUsedCodePermissionRepository dontUsedCodePermissionRepository, UserPermissionRepository userPermissionRepository, GroupRepository groupRepository, @Lazy MessageService messageService, CodeService codeService, TariffRepository tariffRepository, CodeGroupRepository codeGroupRepository, ScreenshotGroupRepository screenshotGroupRepository, JoinGroupRequestRepository joinGroupRequestRepository) {
        this.commonUtils = commonUtils;
        this.buttonService = buttonService;
        this.tempData = tempData;
        this.ownerBotSender = ownerBotSender;
        this.dontUsedCodePermissionRepository = dontUsedCodePermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.groupRepository = groupRepository;
        this.messageService = messageService;
        this.codeService = codeService;
        this.tariffRepository = tariffRepository;
        this.codeGroupRepository = codeGroupRepository;
        this.screenshotGroupRepository = screenshotGroupRepository;
        this.joinGroupRequestRepository = joinGroupRequestRepository;
    }

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
            } else if (data.startsWith(AppConstant.MANAGE_GROUP_TARIFF_DATA)) {
                manageGroupPrice(callbackQuery);
            } else if (data.startsWith(AppConstant.MANAGE_GROUP_PAYMENT_DATA)) {
                showGroupPaymentInfo(callbackQuery);
            } else if (data.startsWith(AppConstant.ADD_CARD_NUMBER_DATA)) {
                addCardNumber(callbackQuery);
            } else if (data.startsWith(AppConstant.CHANGE_TO_ONE_CARD_DATA)) {
                changeToOneCard(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.SETTINGS_PAYMENT) || user.getState().equals(StateEnum.SETTINGS_TARIFF)) {
            if (callbackQuery.getData().startsWith("true:") || callbackQuery.getData().startsWith("false")) {
                changeStatus(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                showManageGroupInfo(callbackQuery);
            } else if ((data.startsWith(AppConstant.ADD_TARIFF_DATA))) {
                addTariff(callbackQuery);
            } else if (data.startsWith(AppConstant.TARIFF_DATA)) {
                showSettingsTariff(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.GENERATE_CODE_FOR_GROUP)) {
            if (data.startsWith(AppConstant.GENERATE_CODE_FOR_GROUP)) {
                showGroupTariffs(callbackQuery);
            } else if (data.startsWith(AppConstant.SELECT_TARIFF_FOR_GENERATE_CODE)) {
                generateCodeWithTariff(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backToGroupList(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.MANAGE_TARIFF)) {
            if (data.startsWith(AppConstant.BACK_DATA)) {
                backToTariffList(callbackQuery);
            } else if (data.startsWith(AppConstant.DELETE_TARIFF_DATA)) {
                deleteTariff(callbackQuery);
            } else if (data.startsWith(AppConstant.CHANGE_TARIFF_DAY_DATA)) {
                changeTariffExpire(callbackQuery);
            } else if (data.startsWith(AppConstant.CHANGE_TARIFF_NAME_DATA)) {
                changeTariffName(callbackQuery);
            } else if (data.startsWith(AppConstant.CHANGE_TARIFF_ORDER_DATA)) {
                changeTariffOrder(callbackQuery);
            } else if (data.startsWith(AppConstant.CHANGE_TARIFF_PRICE_DATA)) {
                changeTariffPrice(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.SELECT_GROUP_FOR_SCREENSHOT)) {
            sendAllScreenshots(callbackQuery);
        } else if (user.getState().equals(StateEnum.ON_SCREENSHOTS)) {
            if (data.startsWith(AppConstant.ACCEPT_SCREENSHOT_DATA)) {
                acceptReq(callbackQuery);
            } else if (data.startsWith(AppConstant.REJECT_SCREENSHOT_DATA)) {
                rejectReq(callbackQuery);
            }
        }
    }

    private void addTariffToTemp(CallbackQuery callbackQuery, StateEnum stateEnum, String text) {
        Long userId = callbackQuery.getFrom().getId();
        ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        commonUtils.setState(userId, stateEnum);
        ownerBotSender.exe(userId, text, null);
        long tariffId = Long.parseLong(callbackQuery.getData().split("\\+")[0].split(":")[1]);
        tempData.addTempTariff(userId, tariffRepository.findById(tariffId).orElseThrow());
    }

    private void changeTariffPrice(CallbackQuery callbackQuery) {
        addTariffToTemp(callbackQuery, StateEnum.CHANGE_TARIFF_PRICE, AppConstant.SEND_TARIFF_PRICE);
    }

    private void changeTariffOrder(CallbackQuery callbackQuery) {
        addTariffToTemp(callbackQuery, StateEnum.CHANGE_TARIFF_ORDER, AppConstant.SEND_TARIFF_ORDER);
    }

    private void changeTariffName(CallbackQuery callbackQuery) {
        addTariffToTemp(callbackQuery, StateEnum.CHANGE_TARIFF_NAME, AppConstant.SEND_TARIFF_NAME);
    }

    private void changeTariffExpire(CallbackQuery callbackQuery) {
        addTariffToTemp(callbackQuery, StateEnum.CHANGE_TARIFF_EXPIRE, AppConstant.SEND_TARIFF_EXPIRE);
    }

    private void deleteTariff(CallbackQuery callbackQuery) {
        long tariffId = Long.parseLong(callbackQuery.getData().split("\\+")[0].split(":")[1]);
        tariffRepository.findById(tariffId).ifPresent(tariffRepository::delete);
        callbackQuery.setData(callbackQuery.getData().split("\\+")[1]);
        manageGroupPrice(callbackQuery);
    }

    private void changeToOneCard(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        List<Group> groups = groupRepository.findAllByOwnerId(userId);
        Integer messageId = callbackQuery.getMessage().getMessageId();
        ownerBotSender.deleteMessage(userId, messageId);
        if (groups.isEmpty() || groups.size() == 1) {
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, null);
            return;
        }
        ownerBotSender.exe(userId, AppConstant.SEND_CARD_NUMBER, null);
        commonUtils.setState(userId, StateEnum.SENDING_CARD_NUMBER_FOR_ALL);
    }

    private void rejectReq(CallbackQuery callbackQuery) {
        rejAccRequests(callbackQuery, ScreenshotStatus.REJECT, AppConstant.REJECTED_TEXT, false);
    }

    private void rejAccRequests(CallbackQuery callbackQuery, ScreenshotStatus status, String text, boolean isActive) {
        String data = callbackQuery.getData();
        String[] split = data.split("\\+");
        long userId = Long.parseLong(split[0].split(":")[1]);
        long groupId = Long.parseLong(split[1].split(":")[1]);
        long screenshotId = Long.parseLong(split[2].split(":")[1]);
        Optional<JoinGroupRequest> optionalRequest = joinGroupRequestRepository.findByUserIdAndGroupId(userId, groupId);
        if (optionalRequest.isEmpty()) {
            ownerBotSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        ScreenshotGroup screenshotGroup = screenshotGroupRepository.findById(screenshotId).orElseThrow();
        screenshotGroup.setActive(isActive);
        screenshotGroup.setActiveDate(new Timestamp(System.currentTimeMillis()));
        screenshotGroup.setStatus(status);
        screenshotGroupRepository.saveOptional(screenshotGroup);
        JoinGroupRequest joinGroupRequest = optionalRequest.get();
        if (data.startsWith(AppConstant.REJECT_SCREENSHOT_DATA))
            ownerBotSender.revokeJoinRequest(joinGroupRequest);
        else
            ownerBotSender.acceptRequest(joinGroupRequest);

        joinGroupRequestRepository.delete(joinGroupRequest);
        ownerBotSender.changeCaption(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), text);

    }

    private void acceptReq(CallbackQuery callbackQuery) {
        rejAccRequests(callbackQuery, ScreenshotStatus.ACCEPT, AppConstant.ACCEPTED_TEXT, true);
    }

    private void sendAllScreenshots(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        List<ScreenshotGroup> screenshots = screenshotGroupRepository.findAllByGroupIdAndStatus(groupId, ScreenshotStatus.DONT_SEE);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (screenshots.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.HAVE_NOY_ANY_SCREENSHOT, buttonService.startButton(userId));
            return;
        }
        for (ScreenshotGroup screenshot : screenshots) {
            SendPhoto sendPhoto = new SendPhoto();
            InputFile inputFile = new InputFile();
            String fileIdOrUrl = screenshot.getPath();
            inputFile.setMedia(new File(fileIdOrUrl));
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setChatId(userId);
            sendPhoto.setCaption(AppConstant.DONT_SEE);
            Map<String, String> map = getScreenshotData(screenshot);
            InlineKeyboardMarkup replyMarkup = buttonService.callbackKeyboard(List.of(map), 1, false);
            sendPhoto.setReplyMarkup(replyMarkup);
            try {
                ownerBotSender.execute(sendPhoto);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        ownerBotSender.deleteMessage(userId, messageId);
        commonUtils.setState(userId, StateEnum.ON_SCREENSHOTS);
        ownerBotSender.exe(userId, AppConstant.FOR_BUTTON_SEND_START, null);
    }

    private static Map<String, String> getScreenshotData(ScreenshotGroup screenshot) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(AppConstant.ACCEPT_SCREENSHOT_TEXT, AppConstant.ACCEPT_SCREENSHOT_DATA + screenshot.getSendUserId() + "+" + AppConstant.GROUP_DATA + screenshot.getGroupId() + "+" + AppConstant.GROUP_DATA + screenshot.getId());
        map.put(AppConstant.REJECT_SCREENSHOT_TEXT, AppConstant.REJECT_SCREENSHOT_DATA + screenshot.getSendUserId() + "+" + AppConstant.GROUP_DATA + screenshot.getGroupId() + "+" + AppConstant.GROUP_DATA + screenshot.getId());
        return map;
    }

    private void addCardNumber(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        commonUtils.setState(userId, StateEnum.SENDING_CARD_NUMBER_FOR_ONE);
        ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
        String text = "У вас нет карты на этом групп или канал вот пример: 8600 0000 0000 0000";
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Optional<Group> byGroupId = groupRepository.findByGroupId(groupId);
        if (byGroupId.isPresent()) {
            Group group = byGroupId.get();
            if (checkString(group.getCardNumber())) {
                text = "У вас стоит %s карта если желате измечить то отправту другой номер карты пример 8600 0000 0000 0000 если не желаете изменить то жмите на /start".formatted(group.getCardNumber());
            }
        }
        tempData.addTempGroupId(userId, groupId);
        ownerBotSender.exe(userId, text, null);
    }

    private void backToTariffList(CallbackQuery callbackQuery) {
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.SETTINGS_TARIFF);
        callbackQuery.setData(callbackQuery.getData().split("\\+")[1]);
        manageGroupPrice(callbackQuery);
    }

    private void showSettingsTariff(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        long tariffId = Long.parseLong(split[0].split(":")[1]);
        long groupId = Long.parseLong(split[1].split(":")[1]);
        Optional<Tariff> optionalTariff = tariffRepository.findById(tariffId);
        Long userId = callbackQuery.getFrom().getId();
        if (optionalTariff.isEmpty()) {
            if (callbackQuery.getMessage() != null) {
                Integer messageId = callbackQuery.getMessage().getMessageId();
                ownerBotSender.deleteMessage(userId, messageId);
                ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            }
            return;
        }
        Tariff tariff = optionalTariff.get();
        String sb = "Информация по тарифу\nНазвания канала или груп - " + tariff.getGroup().getName() + "\n\n" +
                "Названия тарифа - " + tariff.getName() + "\n" +
                "Скок дней оно действует - " + tariff.getDays() + "\n" +
                "Цена - " + tariff.getPrice() + "\n" +
                "По очереди - " + tariff.getOrderBy() + "\n";
        List<Map<String, String>> list = new ArrayList<>();
        list.add(Map.of(AppConstant.CHANGE_TARIFF_NAME_TEXT,
                AppConstant.CHANGE_TARIFF_NAME_DATA + tariffId + "+" + AppConstant.GROUP_DATA + groupId));

        list.add(Map.of(AppConstant.CHANGE_TARIFF_DAY_TEXT,
                AppConstant.CHANGE_TARIFF_DAY_DATA + tariffId + "+" + AppConstant.GROUP_DATA + groupId));

        list.add(Map.of(
                AppConstant.CHANGE_TARIFF_PRICE_TEXT,
                AppConstant.CHANGE_TARIFF_PRICE_DATA + tariffId + "+" + AppConstant.GROUP_DATA + groupId));

        if (tariff.getGroup().getTariffs().size() != 1) {
            list.add(Map.of(
                    AppConstant.CHANGE_TARIFF_ORDER_TEXT,
                    AppConstant.CHANGE_TARIFF_ORDER_DATA + tariffId + "+" + AppConstant.GROUP_DATA + groupId));
        }

        list.add(Map.of(
                AppConstant.DELETE_TARIFF_TEXT,
                AppConstant.DELETE_TARIFF_DATA + tariffId + "+" + AppConstant.GROUP_DATA + groupId));

        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + "+" + AppConstant.GROUP_DATA + groupId));
        commonUtils.setState(userId, StateEnum.MANAGE_TARIFF);
        InlineKeyboardMarkup keyboardMarkup = buttonService.callbackKeyboard(list, 1, false);
        if (callbackQuery.getMessage() != null) {
            Integer messageId = callbackQuery.getMessage().getMessageId();
            ownerBotSender.changeText(userId, messageId, sb);
            ownerBotSender.changeKeyboard(userId, messageId, keyboardMarkup);
            return;
        }
        ownerBotSender.exe(userId, sb, keyboardMarkup);
    }


    private void backToGroupList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        SendMessage sendMessage = messageService.generateCodeListGroups(userId);
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (sendMessage == null) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
        }
        assert sendMessage != null;
        ownerBotSender.changeText(userId, messageId, sendMessage.getText());
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) sendMessage.getReplyMarkup());

    }

    private void generateCodeWithTariff(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split("\\+");
        long tariffId = Long.parseLong(split[0].split(":")[1]);
        long groupId = Long.parseLong(split[1].split(":")[1]);
        Optional<Tariff> optionalTariff = tariffRepository.findById(tariffId);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (optionalTariff.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        Tariff tariff = optionalTariff.get();
        CodeGroup codeGroup = new CodeGroup(codeService.generateCode(), groupId, null, tariff.getDays(), false, null);
        codeGroupRepository.saveOptional(codeGroup);
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.deleteMessage(userId, messageId);
        ownerBotSender.exe(userId, AppConstant.CODE_FOR_JOIN_REQ.formatted(tariff.getGroup().getName(), tariff.getName(), codeGroup.getCode()), buttonService.startButton(userId));
    }

    private void showGroupTariffs(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (optionalGroup.isEmpty()) {
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        Group group = optionalGroup.get();
        List<Tariff> tariffs = group.getTariffs();
        if (tariffs.isEmpty()) {
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.YOU_HAVE_NOT_ANY_TARIFFS, buttonService.startButton(userId));
            return;
        }
        Collections.sort(tariffs);
        StringBuilder sb = new StringBuilder();
        sb.append("Список тарифи: \n\n=============\nВыберите тариф который вам нужно взят код.\n--------------\n\n");
        List<Map<String, String>> list = new ArrayList<>();
        int i = 1;
        for (Tariff tariff : tariffs) {
            sb.append(i).append(". ").append(tariff.getName()).append("\n").append("--  ").append(tariff.getDays()).append(" дней\n\n");
            list.add(Map.of(tariff.getName(), AppConstant.SELECT_TARIFF_FOR_GENERATE_CODE + tariff.getId() + "+" + callbackQuery.getData()));
        }
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));
        ownerBotSender.changeText(userId, messageId, sb.toString());
        ownerBotSender.changeKeyboard(userId, messageId, buttonService.callbackKeyboard(list, 1, false));
    }

    private void addTariff(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split("\\+")[1].split(":")[1]);
        Tariff tariff = new Tariff();
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        ownerBotSender.deleteMessage(userId, messageId);
        if (optionalGroup.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, messageId);
            return;
        }
        tariff.setGroup(optionalGroup.get());
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
            Long userId = callbackQuery.getFrom().getId();
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
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

    private void manageGroupPrice(CallbackQuery callbackQuery) {
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.SETTINGS_TARIFF);
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
        sb.append(group.getName()).append(": ").append("\n\n").append("-----Тарафи-----");
        int i = 1;
        List<Tariff> tariffs = group.getTariffs();
        if (tariffs!=null && !tariffs.isEmpty()) {

            Collections.sort(tariffs);
            for (Tariff tariff : tariffs) {
                sb.append("\n").append(i++).append(". ").append(tariff.getName()).append("\n-----------");

                Map<String, String> map = new HashMap<>();
                map.put(tariff.getName(), AppConstant.TARIFF_DATA + tariff.getId() + "+" + AppConstant.GROUP_DATA + groupId);
                list.add(map);
            }
        }
        list.add(Map.of(AppConstant.ADD_TARIFF_TEXT, AppConstant.ADD_TARIFF_DATA + "+" + AppConstant.GROUP_DATA + groupId));
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + groupId));
        if (callbackQuery.getMessage() == null) {
            ownerBotSender.exe(userId, sb.toString(), buttonService.callbackKeyboard(list, 1, false));
            return;
        }
        changeText(callbackQuery, sb.toString());
        changeReply(callbackQuery, buttonService.callbackKeyboard(list, 1, false));
    }

    @Override
    public void showManageGroupInfo(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Optional<Group> byGroupId = groupRepository.findByGroupId(groupId);
        Long userId = callbackQuery.getFrom().getId();
        if (byGroupId.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        String sb = byGroupId.get().getName() + ": " + "\n\n" + AppConstant.SELECT_CHOOSE;
        commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
        ReplyKeyboard keyboard = getShowManageGroupInfoKeyboard(groupId);
        changeText(callbackQuery, sb);
        changeReply(callbackQuery, (InlineKeyboardMarkup) keyboard);
    }


    private void changeReply(CallbackQuery callbackQuery, InlineKeyboardMarkup keyboard) {
        ownerBotSender.changeKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), keyboard);
    }

    private void changeText(CallbackQuery callbackQuery, String sb) {
        ownerBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), sb);
    }

    private ReplyKeyboard getShowManageGroupInfoKeyboard(long groupId) {
        List<Map<String, String>> list = List.of(
                Map.of(AppConstant.MANAGE_GROUP_PAYMENT_TEXT,
                        AppConstant.MANAGE_GROUP_PAYMENT_DATA + groupId),
                Map.of(AppConstant.MANAGE_GROUP_TARIFF_TEXT,
                        AppConstant.MANAGE_GROUP_TARIFF_DATA + groupId),
                Map.of(AppConstant.ADD_CARD_NUMBER_TEXT, AppConstant.ADD_CARD_NUMBER_DATA + groupId),
                Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));

        return buttonService.callbackKeyboard(list, 1, false);
    }

    private void backShowGroupList(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        SendMessage groupSettings = buttonService.getGroupSettings(userId);
        Integer messageId = callbackQuery.getMessage().getMessageId();
        if (groupSettings == null) {
            ownerBotSender.deleteMessage(userId, messageId);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
        ownerBotSender.changeText(userId, messageId, groupSettings.getText());
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) groupSettings.getReplyMarkup());
    }

    private void showGroupPaymentInfo(CallbackQuery callbackQuery) {
        long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Long userId = callbackQuery.getFrom().getId();
        commonUtils.setState(userId, StateEnum.SETTINGS_PAYMENT);
        Optional<Group> optionalGroup = groupRepository.findByGroupId(groupId);
        if (optionalGroup.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(optionalGroup.get().getName()).append(": ").append("\n\n");
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
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
        InlineKeyboardMarkup replyKeyboard = buttonService.callbackKeyboard(list, 1, false);
        changeText(callbackQuery, sb.toString());
        changeReply(callbackQuery, replyKeyboard);
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
        Long userId = callbackQuery.getFrom().getId();
        DontUsedCodePermission dontUsedCodePermission = tempData.getTempCode(userId);
        if (i == 1)
            dontUsedCodePermission.setPayment(!dontUsedCodePermission.isPayment());
        if (i == 2)
            dontUsedCodePermission.setCodeGeneration(!dontUsedCodePermission.isCodeGeneration());
        if (i == 3)
            dontUsedCodePermission.setScreenshot(!dontUsedCodePermission.isScreenshot());
        ReplyKeyboard replyKeyboard = buttonService.generateKeyboardPermissionStatus(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot(), true);
        String text = choosePaymentString(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        ownerBotSender.changeText(userId, callbackQuery.getMessage().getMessageId(), text);
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
        Integer messageId = callbackQuery.getMessage().getMessageId();
        ownerBotSender.changeText(userId, messageId, AppConstant.SELECT_ANY_EXPIRE);
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) replyKeyboard);
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

    private boolean checkString(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
}
