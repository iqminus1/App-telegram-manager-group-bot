package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import org.springframework.util.StreamUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.apptelegrammanagergroupbot.entity.JoinGroupRequest;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class AdminBotSender extends DefaultAbsSender {
    private final String token;

    public AdminBotSender(String token) {
        super(new DefaultBotOptions(), token);
        this.token = token;
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

    public void kickChatMember(JoinGroupRequest joinGroupRequest) {
        BanChatMember banChatMember = new BanChatMember(joinGroupRequest.getGroupId().toString(), joinGroupRequest.getUserId());
        try {
            execute(banChatMember);
            UnbanChatMember unbanChatMember = new UnbanChatMember(joinGroupRequest.getGroupId().toString(), joinGroupRequest.getUserId());
            execute(unbanChatMember);
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

    public void changeKeyboard(Long chatId, Integer messageId, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(keyboardMarkup);

        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Error executing change keyboard: " + e.getMessage(), e);
        }
    }


    public String getFilePath(PhotoSize photoSize) {
        GetFile getFile = new GetFile(photoSize.getFileId());
        try {
            File execute = execute(getFile);

            String fileUrl = execute.getFileUrl(token);

            String fileName = UUID.randomUUID().toString();
            String[] split = fileUrl.split("\\.");
            String fileExtension = split[split.length - 1];
            String filePath = fileName + "." + fileExtension;

            Path targetPath = Paths.get(AppConstant.FILE_PATH, filePath);

            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = new URL(fileUrl).openStream();
                 OutputStream outputStream = Files.newOutputStream(targetPath)) {
                StreamUtils.copy(inputStream, outputStream);
            }

            return targetPath.toString();
        } catch (TelegramApiException | IOException e) {
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

    public void exe(Long userId, String text) {
        try {
            execute(new SendMessage(userId.toString(), text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void openChat(Long userId, Long groupId) {
        JoinGroupRequest joinGroupRequest = new JoinGroupRequest(userId, groupId);
        acceptJoinRequest(joinGroupRequest);
        kickChatMember(joinGroupRequest);
    }

    public String getLink(Long groupId) {
        try {
            EditChatInviteLink editChatInviteLink = new EditChatInviteLink();
            editChatInviteLink.setChatId(groupId);
            editChatInviteLink.setName(AdminConstants.LINK_NAME);
            ChatInviteLink execute = execute(editChatInviteLink);
            return execute.getInviteLink();
        } catch (TelegramApiException e) {
            CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink();
            createChatInviteLink.setChatId(groupId);
            createChatInviteLink.setCreatesJoinRequest(true);
            createChatInviteLink.setName(AdminConstants.LINK_NAME);
            ChatInviteLink execute = null;
            try {
                execute = execute(createChatInviteLink);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
            return execute.getInviteLink();
        }
    }
}
