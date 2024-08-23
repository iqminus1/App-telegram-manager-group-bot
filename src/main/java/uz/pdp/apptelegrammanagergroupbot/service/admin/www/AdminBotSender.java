package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.DeclineChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;

public class AdminBotSender extends DefaultAbsSender {
    public AdminBotSender(String token) {
        super(new DefaultBotOptions(), token);
    }

    public void exe(Long userId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptJoinRequest(JoinGroupRequest joinRequest) {
        ApproveChatJoinRequest acceptJoinReq = new ApproveChatJoinRequest();
        acceptJoinReq.setUserId(joinRequest.getUserId());
        acceptJoinReq.setChatId(joinRequest.getGroupId());
        try {
            execute(acceptJoinReq);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptJoinRequest(Long userId, Long groupId) {
        acceptJoinRequest(new JoinGroupRequest(userId, groupId));
    }

    public void revokeJoinRequest(Long userId, Long groupId) {
        revokeJoinRequest(new JoinGroupRequest(userId, groupId));
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

    public void changeKeyboard(Long userId, Integer messageId, ReplyKeyboard keyboardMarkup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(userId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) keyboardMarkup);
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String getChatName(Long chatId) {
        GetChat getChat = new GetChat();
        getChat.setChatId(chatId);
        try {
            return execute(getChat).getTitle();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Long userId, Integer messageId) {
        try {
            execute(new DeleteMessage(userId.toString(), messageId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
