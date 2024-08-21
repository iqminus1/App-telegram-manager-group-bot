package uz.pdp.apptelegrammanagergroupbot.service.admin.www;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import uz.pdp.apptelegrammanagergroupbot.entity.Group;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;
import uz.pdp.apptelegrammanagergroupbot.repository.GroupRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemberServiceImpl implements ChatMemberService {

    private final GroupRepository groupRepository;

    @Override
    public void process(ChatMemberUpdated chatMember, String botUsername, UserPermission userPermission) {
        if (chatMember.getNewChatMember().getUser().getUserName().equals(botUsername)) {
            if (List.of(ChatMemberOwner.STATUS, ChatMemberAdministrator.STATUS).contains(chatMember.getNewChatMember().getStatus())) {
                Group group = new Group(userPermission.getUserId(), chatMember.getChat().getId(), null, userPermission.isPayment(), userPermission.isCode(), userPermission.isScreenshot(), null);
                groupRepository.save(group);
            } else if (List.of(ChatMemberMember.STATUS, ChatMemberLeft.STATUS).contains(chatMember.getNewChatMember().getStatus())) {
                groupRepository.findByGroupId(chatMember.getChat().getId()).ifPresent(groupRepository::delete);
            }
        }
    }
}
