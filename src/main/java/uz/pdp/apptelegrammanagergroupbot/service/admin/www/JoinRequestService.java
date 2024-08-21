package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;

public interface JoinRequestService {
    void process(ChatJoinRequest chatJoinRequest);
}
