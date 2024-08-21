package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;
import uz.pdp.apptelegrammanagergroupbot.entity.User;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;
import uz.pdp.apptelegrammanagergroupbot.enums.StateEnum;
import uz.pdp.apptelegrammanagergroupbot.repository.DontUsedCodePermissionRepository;
import uz.pdp.apptelegrammanagergroupbot.service.owner.temp.TempData;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;
import uz.pdp.apptelegrammanagergroupbot.utils.CommonUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final TempData tempData;
    private final OwnerBotSender ownerBotSender;
    private final DontUsedCodePermissionRepository dontUsedCodePermissionRepository;

    @Override
    public void process(CallbackQuery callbackQuery) {
        User user = commonUtils.getUser(callbackQuery.getFrom().getId());
        String data = callbackQuery.getData();
        if (user.getState().equals(StateEnum.CHOOSE_PERMISSION)) {
            if (data.startsWith(AppConstant.PERMISSION_CODE_FOR_DATA))
                selectPermissionType(callbackQuery);
        } else if (user.getState().equals(StateEnum.PERMISSION_EXPIRE)) {
            if (data.startsWith(AppConstant.MONTH_DATA)) {
                permissionSetMonth(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backChoosePermissionCodeType(callbackQuery);
            }
        } else if (user.getState().equals(StateEnum.CHOOSES_PAYMENT)) {
            if (data.startsWith(AppConstant.ACCEPT_PERMISSION_DATA)) {
                acceptPermissionGenerate(callbackQuery);
            } else if (data.startsWith("true:") || data.startsWith("false:")) {
                changePermissionStatus(callbackQuery);
            } else if (data.startsWith(AppConstant.BACK_DATA)) {
                backChoosePermissionExpire(callbackQuery);
            }
        }
    }

    private void backChoosePermissionExpire(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.PERMISSION_EXPIRE);
        ownerBotSender.changeText(userId, messageId, AppConstant.SELECT_ANY_EXPIRE);
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) choosePermissionExpire());
    }

    private void changePermissionStatus(CallbackQuery callbackQuery) {
        int i = Integer.parseInt(callbackQuery.getData().split(":")[1]);
        DontUsedCodePermission dontUsedCodePermission = tempData.get(callbackQuery.getFrom().getId());
        if (i == 1)
            dontUsedCodePermission.setPayment(!dontUsedCodePermission.isPayment());
        if (i == 2)
            dontUsedCodePermission.setCodeGeneration(!dontUsedCodePermission.isCodeGeneration());
        if (i == 3)
            dontUsedCodePermission.setScreenshot(!dontUsedCodePermission.isScreenshot());
        ReplyKeyboard replyKeyboard = generateKeyboardPermissionStatus(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        String text = choosePaymentString(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        ownerBotSender.changeText(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), text);
        ownerBotSender.changeKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), (InlineKeyboardMarkup) replyKeyboard);

    }

    private void acceptPermissionGenerate(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        DontUsedCodePermission dontUsedCodePermission = tempData.get(userId);
        dontUsedCodePermission.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        dontUsedCodePermissionRepository.save(dontUsedCodePermission);
        tempData.deleteTempIfAdmin(userId);
        ownerBotSender.deleteMessage(userId,callbackQuery.getMessage().getMessageId());
        ownerBotSender.exe(userId, AppConstant.GETTING_CODE + dontUsedCodePermission.getCode(), buttonService.startButton(userId));


    }

    private void backChoosePermissionCodeType(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.CHOOSE_PERMISSION);
        ownerBotSender.changeText(userId, messageId, AppConstant.GENERATE_CODE_FOR_PERMISSION_TEXT);
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) buttonService.permissionCodeType());
    }

    private void permissionSetMonth(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        commonUtils.setState(userId, StateEnum.CHOOSES_PAYMENT);
        Integer expire = Integer.parseInt(callbackQuery.getData().split(AppConstant.MONTH_DATA)[1]);
        DontUsedCodePermission dontUsedCodePermission = tempData.get(userId);
        dontUsedCodePermission.setExpireDays(expire);
        ReplyKeyboard replyKeyboard = generateKeyboardPermissionStatus(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot());
        ownerBotSender.changeText(userId, messageId, choosePaymentString(dontUsedCodePermission.isPayment(), dontUsedCodePermission.isCodeGeneration(), dontUsedCodePermission.isScreenshot()));
        ownerBotSender.changeKeyboard(userId, messageId, (InlineKeyboardMarkup) replyKeyboard);
    }

    private ReplyKeyboard generateKeyboardPermissionStatus(boolean bool1, boolean bool2, boolean bool3) {
        String data1 = "true:1";
        String text1 = AppConstant.FALSE;
        if (bool1) {
            data1 = "false:1";
            text1 = AppConstant.TRUE;
        }
        String data2 = "true:2";
        String text2 = AppConstant.FALSE;
        if (bool2) {
            data2 = "false:2";
            text2 = AppConstant.TRUE;
        }
        String data3 = "true:3";
        String text3 = AppConstant.FALSE;
        if (bool3) {
            data3 = "false:3";
            text3 = AppConstant.TRUE;
        }

        return buttonService.callbackKeyboard(List.of(
                Map.of(text1, data1),
                Map.of(text2, data2),
                Map.of(text3, data3),
                Map.of(AppConstant.ACCEPT_PERMISSION_TEXT, AppConstant.ACCEPT_PERMISSION_DATA),
                Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA)
        ), 1, false);
    }

    private void selectPermissionType(CallbackQuery callbackQuery) {
        CodeType type = CodeType.valueOf(callbackQuery.getData().split(AppConstant.PERMISSION_CODE_FOR_DATA)[1]);
        Long userId = callbackQuery.getFrom().getId();
        tempData.get(userId).setType(type);


        ReplyKeyboard replyKeyboard = choosePermissionExpire();

        commonUtils.setState(userId, StateEnum.PERMISSION_EXPIRE);
        ownerBotSender.changeText(userId, callbackQuery.getMessage().getMessageId(), AppConstant.SELECT_ANY_EXPIRE);
        ownerBotSender.changeKeyboard(userId, callbackQuery.getMessage().getMessageId(), (InlineKeyboardMarkup) replyKeyboard);
    }

    private ReplyKeyboard choosePermissionExpire() {
        return buttonService.callbackKeyboard(List.of(
                Map.of(AppConstant.ONE_MONTH, AppConstant.MONTH_DATA + 1),
                Map.of(AppConstant.SIX_MONTH, AppConstant.MONTH_DATA + 6),
                Map.of(AppConstant.ONE_YEAR, AppConstant.MONTH_DATA + 12),
                Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA)), 1, false);
    }

    private String choosePaymentString(boolean bool1, boolean bool2, boolean bool3) {
        StringBuilder sb = new StringBuilder();
        sb.append(AppConstant.WHICH_SERVICES).append("\n\n");
        sb.append("1. ").append(AppConstant.ADD_GROUP_WITH_PAYMENT);
        if (bool1)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        sb.append("\n").append("2. ").append(AppConstant.ADD_GROUP_WITH_GENERATION_CODE);
        if (bool2)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        sb.append("\n").append("3. ").append(AppConstant.ADD_GROUP_WITH_SCREENSHOT);

        if (bool3)
            sb.append(AppConstant.TRUE);
        else
            sb.append(AppConstant.FALSE);
        return sb.toString();
    }
}
