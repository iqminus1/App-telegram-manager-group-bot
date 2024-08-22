package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

import java.util.*;

@Service
public class ButtonServiceImpl implements ButtonService {
    private final UserPermissionRepository userPermissionRepository;
    private final GroupRepository groupRepository;
    private final CodePermissionRepository codePermissionRepository;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;
    private final CreatorRepository creatorRepository;
    private final OwnerBotSender ownerBotSender;
    private final MessageService messageService;

    public ButtonServiceImpl(UserPermissionRepository userPermissionRepository, GroupRepository groupRepository, CodePermissionRepository codePermissionRepository, DontUsedCodePermissionRepository dontUsedCodePermissionRepository, CreatorRepository creatorRepository, OwnerBotSender ownerBotSender, @Lazy MessageService messageService) {
        this.userPermissionRepository = userPermissionRepository;
        this.groupRepository = groupRepository;
        this.codePermissionRepository = codePermissionRepository;
        this.dontUsedCodePermissionRepository = dontUsedCodePermissionRepository;
        this.creatorRepository = creatorRepository;
        this.ownerBotSender = ownerBotSender;
        this.messageService = messageService;
    }

    @Override
    public ReplyKeyboard withString(List<String> list, int rowSize) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        int i = 1;
        for (String text : list) {
            row.add(new KeyboardButton(text));
            if (i == rowSize) {
                rows.add(row);
                row = new KeyboardRow();
                i = 0;
            }
            i++;
        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard callbackKeyboard(List<Map<String, String>> textData, int rowSize, boolean isIncremented) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int i = 1;
        for (Map<String, String> map : textData) {

            for (String text : map.keySet()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setCallbackData(map.get(text));
                if (isIncremented) text = i + ". " + text;
                button.setText(text);
                row.add(button);
            }

            if (rowSize % i == 0) {
                rows.add(row);
                row = new ArrayList<>();
                i = 0;
            }
            i++;

        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard withData(List<String> buttons, int rowSize) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int i = 1;
        for (String text : buttons) {
            InlineKeyboardButton button = new InlineKeyboardButton(text);
            button.setCallbackData(text);
            row.add(button);
            if (rowSize % i == 0) {
                rows.add(row);
                row = new ArrayList<>();
                i = 0;
            }
            i++;
        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard startButton(Long userId) {
        List<String> list = new ArrayList<>();

        creatorRepository.findByUserId(userId).ifPresent((u) -> list.add(AppConstant.GENERATE_CODE_FOR_PERMISSION));

        List<DontUsedCodePermission> dontUsedCodes = dontUsedCodePermissionRepository.findAll();
        if (!dontUsedCodes.isEmpty()) {
            list.add(AppConstant.USE_CODE);
        }

        if (!groupRepository.findAllByOwnerId(userId).isEmpty()) {
            list.add(AppConstant.GROUP_SETTINGS);
        }

        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);

        if (optionalUserPermission.isPresent()) {
            String botToken = optionalUserPermission.get().getBotToken();
            if (botToken == null || botToken.isEmpty() || botToken.isBlank())
                list.add(AppConstant.ADD_BOT_TOKEN);
            else list.add(AppConstant.CHANGE_BOT_TOKEN);

            list.add(AppConstant.EXTENSION_OF_RIGHT);
        } else
            list.add(AppConstant.BUY_PERMISSION);


        list.add(AppConstant.ABOUT_BOT);

        return withString(list, 1);
    }

    @Override
    public ReplyKeyboard permissionCodeType() {
        return callbackKeyboard(List.of(
                Map.of(AppConstant.PERMISSION_CODE_FOR_BUY_TEXT,
                        AppConstant.PERMISSION_CODE_FOR_DATA + CodeType.BUY),
                Map.of(AppConstant.PERMISSION_CODE_FOR_EXTENSION_TEXT,
                        AppConstant.PERMISSION_CODE_FOR_DATA + CodeType.EXPLAINS)
        ), 1, false);
    }

    @Override
    public ReplyKeyboard requestContact() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText(AppConstant.REQUEST_CONTACT);
        KeyboardRow row = new KeyboardRow();
        row.add(keyboardButton);
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard generateKeyboardPermissionStatus(boolean bool1, boolean bool2, boolean bool3, boolean addBack) {
        String data1 = "true:1";
        String text1 = AppConstant.FALSE;
        if (bool1) {
            data1 = "false:1";
            text1 = AppConstant.TRUE;
        }
        String data2 = "true:2";
        String text2 = AppConstant.FALSE;
        if (bool2) {
            data2 = "false:2";
            text2 = AppConstant.TRUE;
        }
        String data3 = "true:3";
        String text3 = AppConstant.FALSE;
        if (bool3) {
            data3 = "false:3";
            text3 = AppConstant.TRUE;
        }

        List<Map<String, String>> list = new ArrayList<>(List.of(
                Map.of(text1, data1),
                Map.of(text2, data2),
                Map.of(text3, data3),
                Map.of(AppConstant.ACCEPT_PERMISSION_TEXT, AppConstant.ACCEPT_PERMISSION_DATA)));

        if (addBack)
            list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));
        return this.callbackKeyboard(list, 1, false);
    }

    @Override
    public SendMessage getGroupSettings(Long userId) {
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            ownerBotSender.exe(userId, AppConstant.HAVE_NOT_PERMISSION, startButton(userId));
            return null;
        }
        UserPermission userPermission = optionalUserPermission.get();
        if (!checkString(userPermission.getBotToken()) || !checkString(userPermission.getBotUsername())) {
            ownerBotSender.exe(userId, AppConstant.FULLY_SEND_BOT_TOKEN_OR_USERNAME, null);
            return null;
        }
        List<Group> groups = groupRepository.findAllByOwnerId(userId);
        if (groups.isEmpty()) {
            ownerBotSender.exe(userId, AppConstant.BOT_NOT_FOLLOW_ANY_GROUPS, startButton(userId));
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Ващ список: ").append("\n\n");
        int i = 1;
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : groups) {
            Map<String, String> map = new HashMap<>();
            String groupName = ownerBotSender.getChatName(group.getGroupId());
            sb.append(i).append(". ").append(groupName);
            map.put(groupName, AppConstant.SHOW_GROUP_INFO + group.getGroupId());
            sb.append("\n").append("-----------").append("\n");
//            map.put(AppConstant.MANAGE_GROUP_PRICE_TEXT,
//                    AppConstant.MANAGE_GROUP_PRICE_DATA);
//
//            if (group.isCode()) {
//                map.put(AppConstant.GENERATE_CODE_FOR_REQUEST_TEXT,
//                        AppConstant.GENERATE_CODE_FOR_REQUEST_DATA);
//            }
//            if (group.isScreenShot()) {
//                if (checkString(group.getCardNumber())) {
//                    map.put(AppConstant.SHOW_SCREENSHOTS_TEXT,
//                            AppConstant.SHOW_SCREENSHOTS_DATA);
//                }
//                map.put(AppConstant.ADD_CARD_NUMBER_TEXT,
//                        AppConstant.ADD_CARD_NUMBER_DATA);
//            }
            list.add(map);
        }
//        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA));
        ReplyKeyboard replyKeyboard = callbackKeyboard(list, 1, true);
        SendMessage sendMessage = new SendMessage(userId.toString(), sb.toString());
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }
    private boolean checkString(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
}
