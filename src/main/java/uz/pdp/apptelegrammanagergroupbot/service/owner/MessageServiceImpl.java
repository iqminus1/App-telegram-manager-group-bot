package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.*;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.service.admin.BotController;
import uz.pdp.apptelegrammanagergroupbot.service.owner.temp.TempData;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final UserPermissionRepository userPermissionRepository;
    private final CreatorCodeRepository creatorCodeRepository;
    private final CreatorRepository creatorRepository;
    private final OwnerBotSender ownerBotSender;
    private final CodeService codeService;
    private final MailService mailService;
    private final TempData tempData;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;
    private final CodePermissionRepository codePermissionRepository;
    private final CallbackService callbackService;
    private final BotController botController;
    private final TariffRepository tariffRepository;
    private final GroupRepository groupRepository;

    @Override
    public void process(Message message) {
        User user = commonUtils.getUser(message.getFrom().getId());
        if (message.getChat().getType().equals("private")) {
            if (message.hasText()) {
                String text = message.getText();
                if (text.equalsIgnoreCase(AppConstant.START)) {
                    start(message);
                }
                if (user.getState().equals(StateEnum.START)) {
                    if (text.equals(AppConstant.GENERATE_CODE_FOR_PERMISSION)) {
                        generateCodeForPermission(message);
                    } else if (text.equals(AppConstant.GENERATE_CODE_FOR_CREATOR)) {
                        mailService.sendCodeForCreator();
                    } else if (text.startsWith(AppConstant.DATA_GET_CREATOR)) {
                        getCreator(message);
                    } else if (text.equals(AppConstant.USE_CODE)) {
                        usePermissionCode(message);
                    } else if (List.of(AppConstant.ADD_BOT_TOKEN, AppConstant.CHANGE_BOT_TOKEN).contains(text)) {
                        addOrChangeToken(message);
                    } else if (text.equals(AppConstant.ABOUT_BOT)) {
                        aboutUs(message);
                    } else if (List.of(AppConstant.BUY_PERMISSION, AppConstant.EXTENSION_OF_RIGHT).contains(text)) {
                        buyOrExtensionPermission(message);
                    } else if (text.equals(AppConstant.GROUP_SETTINGS)) {
                        groupSettings(message);
                    } else if (text.equals(AppConstant.GENERATE_CODE_FOR_REQUEST)) {
                        showListGroups(message);
                    } else if (text.equals(AppConstant.SEE_ALL_SCREENSHOTS)) {
                        seeAllScreenshots(message);
                    }
                } else if (user.getState().equals(StateEnum.USE_CODE)) {
                    checkPermissionCodeAndActivate(message);
                } else if (user.getState().equals(StateEnum.ADMIN_SENDING_TOKEN)) {
                    setAdminBotToken(message);
                } else if (user.getState().equals(StateEnum.ADMIN_SENDING_BOT_USERNAME)) {
                    setAdminBotUsername(message);
                } else if (user.getState().equals(StateEnum.SIZE_OF_REQUESTS)) {
                    setSizeOfRequestGenCode(message);
                } else if (user.getState().equals(StateEnum.ADMIN_SENDING_SIZE_OF_REQUESTS)) {
                    setAdminSizeOfRequests(message);
                } else if (user.getState().equals(StateEnum.SENDING_TARIFF_NAME)) {
                    setTariffName(message);
                } else if (user.getState().equals(StateEnum.SENDING_TARIFF_ORDER)) {
                    setTariffOrder(message);
                } else if (user.getState().equals(StateEnum.SENDING_TARIFF_EXPIRE)) {
                    setTariffExpire(message);
                } else if ((user.getState().equals(StateEnum.SENDING_TARIFF_PRICE))) {
                    setTariffPrice(message);
                } else if (user.getState().equals(StateEnum.SENDING_CARD_NUMBER_FOR_ONE)) {
                    addToOneCardNumber(message);
                }
            } else if (message.hasPhoto()) {
                if (user.getState().equals(StateEnum.OWNER_SENDING_PHOTO))
                    savePhoto(message);
            } else if ((message.hasContact())) {
                if (user.getState().equals(StateEnum.ADMIN_SENDING_CONTACT)) {
                    sendingContact(message);
                }
            }
        }
    }

    private void seeAllScreenshots(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        if (!optionalUserPermission.get().isScreenshot()) {
            return;
        }
        List<Group> allByOwnerId = groupRepository.findAllByOwnerId(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Нажмите из какой групп или канах хотите смотрет фото отправки денги:\n\n");
        int i = 1;
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : allByOwnerId) {
            if (group.isScreenShot()) {
                if (allByOwnerId.size() != 1) {
                    sb.append(i).append(". ");
                }
                sb.append(group.getName()).append("\n");
                list.add(Map.of(group.getName(), AppConstant.GROUP_DATA + group.getGroupId()));
            }
        }
        commonUtils.setState(userId, StateEnum.SELECT_GROUP_FOR_SCREENSHOT);
        ownerBotSender.deleteKeyboard(userId);
        ownerBotSender.exe(userId, sb.toString(), buttonService.callbackKeyboard(list, 1, false));
    }

    private void addToOneCardNumber(Message message) {
        Long userId = message.getFrom().getId();
        Long groupId = tempData.getTempGroupId(userId);

        // Найти группу по groupId
        Optional<Group> optional = groupRepository.findByGroupId(groupId);
        if (optional.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(userId));
            return;
        }

        Group group = optional.get();
        String text = message.getText();

        if (text.matches("\\d{16}")) { // Формат без пробелов
            String formattedCardNumber = text.substring(0, 4) + " " +
                    text.substring(4, 8) + " " +
                    text.substring(8, 12) + " " +
                    text.substring(12);
            group.setCardNumber(formattedCardNumber);
            groupRepository.saveOptional(group);

            CallbackQuery callbackQuery = new CallbackQuery();
            callbackQuery.setFrom(message.getFrom());
            callbackQuery.setData(AppConstant.SHOW_GROUP_INFO + ":" + groupId);
            callbackService.showManageGroupInfo(callbackQuery);

            commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
            return;
        } else if (text.matches("\\d{4} \\d{4} \\d{4} \\d{4}")) { // Формат с пробелами
            group.setCardNumber(text);
            groupRepository.saveOptional(group);

            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.SUCCESSFULLY_CHANGED_ONE_CARD, buttonService.startButton(userId));

            CallbackQuery callbackQuery = new CallbackQuery();
            callbackQuery.setFrom(message.getFrom());
            callbackQuery.setData(AppConstant.SHOW_GROUP_INFO + ":" + groupId);
            callbackService.showManageGroupInfo(callbackQuery);

            return;
        }

        // Если формат карты некорректен
        ownerBotSender.exe(userId, "Отправьте номер карты в правильном формате или нажмите на /start, если не хотите изменять.", null);
    }


    private void showListGroups(Message message) {
        Long userId = message.getFrom().getId();
        SendMessage sendMessage = generateCodeListGroups(userId);
        if (sendMessage == null) {
            return;
        }
        try {
            ownerBotSender.deleteKeyboard(userId);
            ownerBotSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SendMessage generateCodeListGroups(Long userId) {
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty() || optionalUserPermission.get().getExpireDate().before(new Date())) {
            ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_YOUR_MISTAKE, buttonService.startButton(userId));
            return null;
        }
        List<Group> allGroups = groupRepository.findAllByOwnerId(userId);
        if (allGroups.isEmpty()) {
            ownerBotSender.exe(userId, AppConstant.YOU_HAVE_NOT_ANY_GROUPS, buttonService.startButton(userId));
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Скисок ващех канал или групп.").append("\n\n");
        int i = 1;
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : allGroups) {
            sb.append(i++).append(". ").append(group.getName()).append("\n");
            list.add(Map.of(group.getName(), AppConstant.GENERATE_CODE_FOR_GROUP + group.getGroupId()));
        }
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(list, 1, false);
        commonUtils.setState(userId, StateEnum.GENERATE_CODE_FOR_GROUP);
        SendMessage sendMessage = new SendMessage(userId.toString(), sb.toString());
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }

    private void setTariffPrice(Message message) {
        Long userId = message.getFrom().getId();
        Tariff tempTariff = tempData.getTempTariff(userId);
        tempTariff.setPrice(Long.parseLong(message.getText()));
        Optional<Group> optional = groupRepository.findByGroupId(tempTariff.getGroup().getGroupId());
        if (optional.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.EXCEPTION, buttonService.startButton(message.getFrom().getId()));
            return;
        }
        List<Tariff> tariffs = optional.get().getTariffs();
        tariffs.stream().filter(tariff -> tariff.getOrderBy() >= tempTariff.getOrderBy()).forEach(tariff -> tariff.setOrderBy(tariff.getOrderBy() + 1));
        tariffRepository.saveAll(tariffs);
        tariffRepository.saveOptional(tempTariff);
        commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(message.getFrom());
        callbackQuery.setData(AppConstant.MANAGE_GROUP_TARIFF_DATA + tempTariff.getGroup().getGroupId());
        callbackService.process(callbackQuery);
    }

    private void setTariffOrder(Message message) {
        Long userId = message.getFrom().getId();
        int order = Integer.parseInt(message.getText());
        Tariff tempTariff = tempData.getTempTariff(userId);
        if (0 < order && order < tempTariff.getGroup().getTariffs().size() + 2) {
            tempTariff.setOrderBy(order);
            commonUtils.setState(userId, StateEnum.SENDING_TARIFF_EXPIRE);
            ownerBotSender.exe(userId, AppConstant.SEND_TARIFF_EXPIRE, null);
            return;
        }
        ownerBotSender.exe(userId, "Не сработала еще раз отправте", null);

    }

    private void setTariffExpire(Message message) {
        Long userId = message.getFrom().getId();
        String text = message.getText();
        Tariff tempTariff = tempData.getTempTariff(userId);
        tempTariff.setDays(Integer.parseInt(text));
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_PRICE);
        ownerBotSender.exe(userId, AppConstant.SEND_TARIFF_PRICE, null);
    }

    private void setTariffName(Message message) {
        String text = message.getText();
        Long userId = message.getFrom().getId();
        Tariff tempTariff = tempData.getTempTariff(userId);
        tempTariff.setName(text);
        List<Tariff> tariffs = tempTariff.getGroup().getTariffs();
        if (tariffs.isEmpty()) {
            tempTariff.setOrderBy(1);
            commonUtils.setState(userId, StateEnum.SENDING_TARIFF_EXPIRE);
            ownerBotSender.exe(userId, AppConstant.SEND_TARIFF_EXPIRE, null);
            return;
        }
        commonUtils.setState(userId, StateEnum.SENDING_TARIFF_ORDER);
        if (tariffs.size() == 1) {
            ownerBotSender.exe(userId, AppConstant.FIRST_OR_SECOND_ORDER, null);
            return;
        }
        ownerBotSender.exe(userId, AppConstant.SEND_TARIFF_ORDER + (tariffs.size() + 1), null);
    }

    private void groupSettings(Message message) {
        Long userId = message.getFrom().getId();
        commonUtils.setState(userId, StateEnum.SETTINGS_GROUP);
        SendMessage sendMessage = buttonService.getGroupSettings(userId);
        if (sendMessage == null) {
            commonUtils.setState(userId, StateEnum.START);
            return;
        }
        try {
            ownerBotSender.deleteKeyboard(userId);
            ownerBotSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void setAdminSizeOfRequests(Message message) {
        Long userId = message.getFrom().getId();
        int sizeOfRequests = Integer.parseInt(message.getText());

        UserPermission tempPermission = tempData.getTempPermission(userId);
        tempPermission.setSizeRequests(sizeOfRequests);
        commonUtils.setState(userId, StateEnum.ADMIN_SELECTING_PAYMENT);
        ownerBotSender.exe(userId, callbackService.choosePaymentString(tempPermission.isPayment(), tempPermission.isCode(), tempPermission.isScreenshot()), buttonService.generateKeyboardPermissionStatus(tempPermission.isPayment(), tempPermission.isCode(), tempPermission.isScreenshot(), false));
    }

    private void setSizeOfRequestGenCode(Message message) {
        Long userId = message.getFrom().getId();

        tempData.addTempCode(userId, new DontUsedCodePermission(userId, codeService.generateCode(), null, null, null, null, Integer.parseInt(message.getText()), true, true, true));

        commonUtils.setState(userId, StateEnum.OWNER_SENDING_PHOTO);
        ownerBotSender.exe(userId, AppConstant.OWNER_SEND_PHOTO, null);
    }

    private void buyOrExtensionPermission(Message message) {
        Long userId = message.getFrom().getId();
        UserPermission userPermission = new UserPermission(userId, message.getFrom().getFirstName(), null, null, null, null, null, false, false, false);
        tempData.addTempPermission(userId, userPermission);
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_SIZE_OF_REQUESTS);
        ownerBotSender.exe(userId, AppConstant.SIZE_OF_REQUESTS, new ReplyKeyboardRemove(true));
    }

    private void setAdminBotUsername(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setBotUsername(message.getText());
        userPermissionRepository.saveOptional(userPermission);
        botController.addAdminBot(userPermission.getBotToken(), userPermission.getBotUsername(), userPermission.getUserId());
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.exe(userId, AppConstant.BOT_TOKEN_COMPLETED, buttonService.startButton(userId));
    }

    private void sendingContact(Message message) {
        Long userId = message.getFrom().getId();
        Contact contact = message.getContact();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setContactNumber(contact.getPhoneNumber());
        userPermissionRepository.saveOptional(userPermission);
        commonUtils.setState(userId, StateEnum.START);
        message.setText(AppConstant.ADD_BOT_TOKEN);
        this.process(message);
    }

    private void setAdminBotToken(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setBotToken(message.getText());
        userPermissionRepository.saveOptional(userPermission);
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_BOT_USERNAME);
        ownerBotSender.exe(userId, AppConstant.ADMIN_SENDING_BOT_USERNAME, null);
    }

    private void addOrChangeToken(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        if (userPermission.getExpireDate().before(new Date())) {
            return;
        }
        if (userPermission.getContactNumber() == null || userPermission.getContactNumber().isEmpty() || userPermission.getContactNumber().isBlank()) {
            getContact(message);
            return;
        }
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_TOKEN);
        ownerBotSender.exe(userId, AppConstant.SENDING_TOKEN, new ReplyKeyboardRemove(true));
    }

    private void getContact(Message message) {
        Long userId = message.getFrom().getId();
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_CONTACT);
        ownerBotSender.exe(userId, AppConstant.SEND_CONTACT_NUMBER, buttonService.requestContact());
    }


    private void aboutUs(Message message) {
        ownerBotSender.exe(message.getFrom().getId(), AppConstant.ABOUT_US_TEXT, null);
    }

    private void checkPermissionCodeAndActivate(Message message) {
        Long userId = message.getFrom().getId();
        List<DontUsedCodePermission> list = dontUsedCodePermissionRepository.findAllByCode(message.getText());
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            Optional<DontUsedCodePermission> first = list.stream().filter(dontUsed -> dontUsed.getType().equals(CodeType.BUY)).findFirst();
            if (first.isEmpty()) {
                commonUtils.setState(userId, StateEnum.START);
                ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_YOUR_MISTAKE, buttonService.startButton(userId));
                return;
            }
            DontUsedCodePermission dontUsedCodePermission = first.get();
            CodePermission codePermission = new CodePermission(dontUsedCodePermission.getCreateBy(), userId, message.getFrom().getFirstName(), null, null, dontUsedCodePermission.getExpireMonth(), dontUsedCodePermission.getSizeRequests(), dontUsedCodePermission.getType(), dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
            codePermissionRepository.saveOptional(codePermission);
            UserPermission userPermission = new UserPermission(userId, message.getFrom().getFirstName(), null, null, null, Timestamp.valueOf(LocalDateTime.now().plusMonths(dontUsedCodePermission.getExpireMonth())), dontUsedCodePermission.getSizeRequests(), dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
            userPermissionRepository.saveOptional(userPermission);
            dontUsedCodePermissionRepository.delete(dontUsedCodePermission);
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_BUY_IS_VALID, buttonService.startButton(userId));
            return;
        }
        Optional<DontUsedCodePermission> first = list.stream().filter(dontUsed -> dontUsed.getType().equals(CodeType.EXPLAINS)).findFirst();
        if (first.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_YOUR_MISTAKE, buttonService.startButton(userId));
            return;
        }
        DontUsedCodePermission dontUsedCodePermission = first.get();
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setExpireDate(Timestamp.valueOf(userPermission.getExpireDate().toLocalDateTime().plusMonths(dontUsedCodePermission.getExpireMonth())));
        userPermission.setPayment(dontUsedCodePermission.isPayment());
        userPermission.setCode(dontUsedCodePermission.isCodeGeneration());
        userPermission.setScreenshot(dontUsedCodePermission.isScreenshot());
        userPermission.setSizeRequests(dontUsedCodePermission.getSizeRequests());
        CodePermission codePermission = new CodePermission(dontUsedCodePermission.getCreateBy(), userId, message.getFrom().getFirstName(), null, null, dontUsedCodePermission.getExpireMonth(), dontUsedCodePermission.getSizeRequests(), dontUsedCodePermission.getType(), dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        codePermissionRepository.saveOptional(codePermission);
        userPermissionRepository.saveOptional(userPermission);
        dontUsedCodePermissionRepository.delete(dontUsedCodePermission);
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_EXPLAINS_IS_VALID, buttonService.startButton(userId));
    }

    private void usePermissionCode(Message message) {
        commonUtils.setState(message.getFrom().getId(), StateEnum.USE_CODE);
        ownerBotSender.exe(message.getFrom().getId(), AppConstant.SEND_CODE_TEXT, new ReplyKeyboardRemove(true));
    }

    private void savePhoto(Message message) {
        String fileId = message.getPhoto().get(0).getFileId();
        Long userId = message.getFrom().getId();
        DontUsedCodePermission tempCode = tempData.getTempCode(userId);
        tempCode.setPath(fileId);

        ReplyKeyboard replyKeyboard = buttonService.permissionCodeType();
        ownerBotSender.exe(userId, AppConstant.GENERATE_CODE_FOR_PERMISSION_TEXT, replyKeyboard);
        commonUtils.setState(userId, StateEnum.CHOOSE_PERMISSION);
    }

    private void generateCodeForPermission(Message message) {
        Long userId = message.getFrom().getId();
        if (creatorRepository.findByUserId(userId).isEmpty()) {
            return;
        }

        commonUtils.setState(userId, StateEnum.SIZE_OF_REQUESTS);
        ownerBotSender.exe(userId, AppConstant.SIZE_OF_REQUESTS, new ReplyKeyboardRemove(true));
    }

    private void getCreator(Message message) {
        String code = message.getText().split(AppConstant.DATA_SPLIT_FOR_CREATOR)[1];
        Optional<CreatorCode> optionalCreatorCode = creatorCodeRepository.findByCode(code);
        if (optionalCreatorCode.isEmpty()) {
            return;
        }
        creatorCodeRepository.delete(optionalCreatorCode.get());

        Long userId = message.getFrom().getId();
        creatorRepository.saveOptional(new Creator(userId));
        ownerBotSender.exe(userId, AppConstant.CHANGED_TO_CREATOR, null);
    }


    private void start(Message message) {
        Long userId = message.getFrom().getId();
        commonUtils.setState(userId, StateEnum.START);
        ReplyKeyboard replyKeyboard = buttonService.startButton(userId);
        ownerBotSender.exe(userId, AppConstant.START_TEXT, replyKeyboard);
        tempData.removeTempDataByUser(userId);
    }


}
