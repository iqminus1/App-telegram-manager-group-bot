package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface AdminCallbackService {
    void process(CallbackQuery callbackQuery,Long adminId);
}
