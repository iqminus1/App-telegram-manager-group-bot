package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.repository.*;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ButtonServiceImpl implements ButtonService {
    private final UserPermissionRepository userPermissionRepository;
    private final GroupRepository groupRepository;
    private final CodePermissionRepository codePermissionRepository;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;
    private final CreatorRepository creatorRepository;

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

//            if (optionalUserPermission.get().isCode())
//                list.add(AppConstant.GENERATE_CODE_FOR_REQUEST);

//            if (optionalUserPermission.get().isScreenshot())
//                list.add(AppConstant.SEE_ALL_SCREENSHOTS);

//            if (optionalUserPermission.get().isPayment())
//                list.add(AppConstant.VIEW_STATS);

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
}
