package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AdminMessageService {
    void process(Message message, Long adminId);

    SendMessage showRequestLists(Long chatId, Long adminId);
}
