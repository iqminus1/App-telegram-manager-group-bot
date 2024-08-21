package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;
import java.util.Map;

public interface AdminButtonService {
    ReplyKeyboard withString(List<String> list, int rowSize);

    ReplyKeyboard callbackKeyboard(List<Map<String, String>> textData, int rowSize, boolean isIncremented);

}
