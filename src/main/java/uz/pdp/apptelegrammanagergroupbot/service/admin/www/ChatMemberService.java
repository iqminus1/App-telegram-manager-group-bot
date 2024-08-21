package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;

public interface ChatMemberService {
    void process(ChatMemberUpdated chatMember, String botUsername, UserPermission userPermission);
}
