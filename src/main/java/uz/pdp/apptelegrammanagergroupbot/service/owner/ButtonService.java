package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;
import java.util.Map;

public interface ButtonService {
    ReplyKeyboard withString(List<String> list, int rowSize);

    ReplyKeyboard callbackKeyboard(List<Map<String, String>> textData, int rowSize, boolean isIncremented);

    ReplyKeyboard withData(List<String> buttons, int rowSize);

    ReplyKeyboard startButton(Long userId);

    ReplyKeyboard permissionCodeType();
    ReplyKeyboard requestContact();
    ReplyKeyboard generateKeyboardPermissionStatus(boolean bool1, boolean bool2, boolean bool3, boolean addBack);
}
