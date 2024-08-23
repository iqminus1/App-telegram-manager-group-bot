package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageService {
    void process(Message message);
    SendMessage generateCodeListGroups(Long userId);
}
