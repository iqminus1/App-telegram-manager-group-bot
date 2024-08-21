package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Contact;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
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
                } else if (text.equals(AppConstant.BUY_PERMISSION)) {
                    buyPermission(message);
                } else if (List.of(AppConstant.ADD_BOT_TOKEN, AppConstant.CHANGE_BOT_TOKEN).contains(text)) {
                    addOrChangeToken(message);
                } else if (text.equals(AppConstant.ABOUT_BOT)) {
                    aboutUs(message);
                }
            } else if (user.getState().equals(StateEnum.USE_CODE)) {
                checkPermissionCodeAndActivate(message);
            } else if (user.getState().equals(StateEnum.ADMIN_SENDING_TOKEN)) {
                setAdminBotToken(message);
            } else if ((user.getState().equals(StateEnum.ADMIN_SENDING_BOT_USERNAME))) {
                setAdminBotUsername(message);
            }
        } else if (message.hasPhoto()) {
            if (user.getState().equals(StateEnum.OWNER_SENDING_PHOTO))
                savePhoto(message);
        } else if ((message.hasContact())) {
            if (user.getState().equals(StateEnum.ADMIN_SENDING_CONTACT)) {
                sendingContact(message);
            }
        }
    }

    private void setAdminBotUsername(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setBotUsername(message.getText());
        userPermissionRepository.save(userPermission);
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.exe(userId, AppConstant.BOT_TOKEN_COMPLETED, buttonService.startButton(userId));
    }

    private void sendingContact(Message message) {
        Long userId = message.getFrom().getId();
        Contact contact = message.getContact();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setContactNumber(contact.getPhoneNumber());
        userPermissionRepository.save(userPermission);
        commonUtils.setState(userId, StateEnum.START);
        message.setText(AppConstant.ADD_BOT_TOKEN);
        this.process(message);
    }

    private void setAdminBotToken(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setBotToken(message.getText());
        userPermissionRepository.save(userPermission);
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_BOT_USERNAME);
        ownerBotSender.exe(userId, AppConstant.ADMIN_SENDING_BOT_USERNAME, null);
    }

    private void addOrChangeToken(Message message) {
        Long userId = message.getFrom().getId();
        Optional<UserPermission> optionalUserPermission = userPermissionRepository.findByUserId(userId);
        if (optionalUserPermission.isEmpty()) {
            return;
        }
        UserPermission userPermission = optionalUserPermission.get();
        if (userPermission.getExpireDate().before(new Date())) {
            return;
        }
        if (userPermission.getContactNumber() == null || userPermission.getContactNumber().isEmpty() || userPermission.getContactNumber().isBlank()) {
            getContact(message);
            return;
        }
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_TOKEN);
        ownerBotSender.exe(userId, AppConstant.SENDING_TOKEN, new ReplyKeyboardRemove(true));
    }

    private void getContact(Message message) {
        Long userId = message.getFrom().getId();
        commonUtils.setState(userId, StateEnum.ADMIN_SENDING_CONTACT);
        ownerBotSender.exe(userId, AppConstant.SEND_CONTACT_NUMBER, buttonService.requestContact());
    }

    private void buyPermission(Message message) {
        ownerBotSender.exe(message.getFrom().getId(), AppConstant.DONT_COMPLETED, null);
    }

    private void aboutUs(Message message) {
        ownerBotSender.exe(message.getFrom().getId(), AppConstant.ABOUT_US_TEXT, null);
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
            UserPermission userPermission = new UserPermission(userId, message.getFrom().getFirstName(), null, null, null, Timestamp.valueOf(LocalDateTime.now().plusMonths(dontUsedCodePermission.getExpireMonth())), dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
            userPermissionRepository.save(userPermission);
            dontUsedCodePermissionRepository.delete(dontUsedCodePermission);
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_BUY_IS_VALID, buttonService.startButton(userId));
            return;
        }
        Optional<DontUsedCodePermission> first = list.stream().filter(dontUsed -> dontUsed.getType().equals(CodeType.EXPLAINS)).findFirst();
        if (first.isEmpty()) {
            commonUtils.setState(userId, StateEnum.START);
            ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_YOUR_MISTAKE, buttonService.startButton(userId));
            return;
        }
        DontUsedCodePermission dontUsedCodePermission = first.get();
        UserPermission userPermission = optionalUserPermission.get();
        userPermission.setExpireDate(Timestamp.valueOf(userPermission.getExpireDate().toLocalDateTime().plusMonths(dontUsedCodePermission.getExpireMonth())));
        userPermission.setPayment(dontUsedCodePermission.isPayment());
        userPermission.setCode(dontUsedCodePermission.isCodeGeneration());
        userPermission.setScreenshot(dontUsedCodePermission.isScreenshot());
        userPermissionRepository.save(userPermission);
        dontUsedCodePermissionRepository.delete(dontUsedCodePermission);
        commonUtils.setState(userId, StateEnum.START);
        ownerBotSender.exe(userId, AppConstant.PERMISSION_CODE_EXPLAINS_IS_VALID, buttonService.startButton(userId));
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
        commonUtils.setState(userId, StateEnum.START);
        ReplyKeyboard replyKeyboard = buttonService.startButton(userId);
        ownerBotSender.exe(userId, AppConstant.START_TEXT, replyKeyboard);
        tempData.deleteTempIfAdmin(userId);
    }


}
