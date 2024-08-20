package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

@Component
public class OwnerBot extends TelegramLongPollingBot {
    private final MessageService messageService;
    private final CallbackService callbackService;

    public OwnerBot(MessageService messageService, CallbackService callbackService) {
        super(new DefaultBotOptions(), AppConstant.BOT_TOKEN);
        this.messageService = messageService;
        this.callbackService = callbackService;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            messageService.process(update.getMessage());
        } else if (update.hasCallbackQuery())
            callbackService.process(update.getCallbackQuery());
    }

    @Override
    public String getBotUsername() {
        return AppConstant.BOT_USERNAME;
    }
}
