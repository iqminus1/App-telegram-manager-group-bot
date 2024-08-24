package uz.pdp.apptelegrammanagergroupbot.service.owner;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

@Component
public class OwnerBotSender extends DefaultAbsSender {
    public OwnerBotSender() {
        super(new DefaultBotOptions(), AppConstant.BOT_TOKEN);
    }


    public void deleteMessage(Long userId, Integer messageId) {
        try {
            execute(new DeleteMessage(userId.toString(), messageId));

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void exe(Long userId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(userId);
        sendMessage.setReplyMarkup(keyboard);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeCaption(Long userId, Integer messageId, String text) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setCaption(text);
        editMessageCaption.setChatId(userId);
        editMessageCaption.setMessageId(messageId);
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeText(Long userId, Integer messageId, String text) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setChatId(userId);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeKeyboard(Long userId, Integer messageId, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(userId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(keyboardMarkup);
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteKeyboard(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(".");
        sendMessage.setChatId(userId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        try {
            Message execute = execute(sendMessage);
            Integer messageId = execute.getMessageId();
            deleteMessage(userId, messageId);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptRequest(JoinGroupRequest joinRequest) {
        ApproveChatJoinRequest acceptJoinReq = new ApproveChatJoinRequest();
        acceptJoinReq.setUserId(joinRequest.getUserId());
        acceptJoinReq.setChatId(joinRequest.getGroupId());
        try {
            execute(acceptJoinReq);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void revokeJoinRequest(JoinGroupRequest joinRequest) {
        DeclineChatJoinRequest declineChatJoinRequest = new DeclineChatJoinRequest();
        declineChatJoinRequest.setChatId(joinRequest.getGroupId());
        declineChatJoinRequest.setUserId(joinRequest.getUserId());
        try {
            execute(declineChatJoinRequest);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
