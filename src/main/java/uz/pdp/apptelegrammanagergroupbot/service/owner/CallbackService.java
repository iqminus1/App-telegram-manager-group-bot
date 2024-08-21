package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackService {
    void process(CallbackQuery callbackQuery);

    String choosePaymentString(boolean bool1, boolean bool2, boolean bool3);
}
