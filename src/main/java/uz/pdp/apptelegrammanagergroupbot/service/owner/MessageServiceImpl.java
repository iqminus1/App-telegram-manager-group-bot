package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.pdp.apptelegrammanagergroupbot.entity.*;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.CreatorCodeRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.CreatorRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.DontUsedCodePermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.repository.UserPermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.service.owner.temp.TempData;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final UserPermissionRepository userPermissionRepository;
    private final CreatorCodeRepository creatorCodeRepository;
    private final CreatorRepository creatorRepository;
    private final OwnerBotSender ownerBotSender;
    private final CodeService codeService;
    private final MailService mailService;
    private final TempData tempData;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;

    @Override
    public void process(Message message) {
        User user = commonUtils.getUser(message.getFrom().getId());
        if (message.hasText()) {
            String text = message.getText();
            if (text.equalsIgnoreCase(AppConstant.START)) {
                start(message);
            }
            if (user.getState().equals(StateEnum.START)) {
                if (text.equals(AppConstant.GENERATE_CODE_FOR_PERMISSION)) {
                    generateCodeForPermission(message);
                } else if (text.equals(AppConstant.GENERATE_CODE_FOR_CREATOR))
                    mailService.sendCodeForCreator();
                else if (text.startsWith(AppConstant.DATA_GET_CREATOR))
                    getCreator(message);
                else if (text.equals(AppConstant.USE_CODE)) {
                    usePermissionCode(message);
                }
            } else if (user.getState().equals(StateEnum.USE_CODE)) {
                checkPermissionCodeAndActivate(message);
            }
        } else if (message.hasPhoto()) {
            if (user.getState().equals(StateEnum.OWNER_SENDING_PHOTO))
                savePhoto(message);
        }
    }

    private void checkPermissionCodeAndActivate(Message message) {
        Long userId = message.getFrom().getId();
        List<DontUsedCodePermission> list = dontUsedCodePermissionRepository.findAllByCode(message.getText());
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            Optional<DontUsedCodePermission> first = list.stream().filter(dontUsed -> dontUsed.getType().equals(CodeType.BUY)).findFirst();
            if (first.isEmpty()) {
                commonUtils.setState(userId, StateEnum.START);
                ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_YOUR_MISTAKE, buttonService.startButton(userId));
                return;
            }
            DontUsedCodePermission dontUsedCodePermission = first.get();
//            new UserPermission(userId, message.getFrom().getFirstName(), null, null, null, LocalDateTime.now().plusMonths(dontUsedCodePermission.getExpireDays());

        }
    }

    private void usePermissionCode(Message message) {
        commonUtils.setState(message.getFrom().getId(), StateEnum.USE_CODE);
        ownerBotSender.exe(message.getFrom().getId(), AppConstant.SEND_CODE_TEXT, new ReplyKeyboardRemove(true));
    }

    private void savePhoto(Message message) {
        String fileId = message.getPhoto().get(0).getFileId();
        Long userId = message.getFrom().getId();
        tempData.addTempCode(userId, new DontUsedCodePermission(userId, codeService.generateCode(), fileId, null, null, null, false, false, false));

        ReplyKeyboard replyKeyboard = buttonService.permissionCodeType();
        ownerBotSender.exe(userId, AppConstant.GENERATE_CODE_FOR_PERMISSION_TEXT, replyKeyboard);
        commonUtils.setState(userId, StateEnum.CHOOSE_PERMISSION);
    }

    private void generateCodeForPermission(Message message) {
        Long userId = message.getFrom().getId();
        if (creatorRepository.findByUserId(userId).isEmpty()) {
            return;
        }

        commonUtils.setState(userId, StateEnum.OWNER_SENDING_PHOTO);
        ownerBotSender.exe(userId, AppConstant.OWNER_SEND_PHOTO, new ReplyKeyboardRemove(true));
    }

    private void getCreator(Message message) {
        String code = message.getText().split(AppConstant.DATA_SPLIT_FOR_CREATOR)[1];
        Optional<CreatorCode> optionalCreatorCode = creatorCodeRepository.findByCode(code);
        if (optionalCreatorCode.isEmpty()) {
            return;
        }
        creatorCodeRepository.delete(optionalCreatorCode.get());

        Long userId = message.getFrom().getId();
        creatorRepository.save(new Creator(userId));
        ownerBotSender.exe(userId, AppConstant.CHANGED_TO_CREATOR, null);
    }


    private void start(Message message) {
        Long userId = message.getFrom().getId();
        ReplyKeyboard replyKeyboard = buttonService.startButton(userId);
        ownerBotSender.exe(userId, AppConstant.START_TEXT, replyKeyboard);
        tempData.deleteTempIfAdmin(userId);
    }


}
