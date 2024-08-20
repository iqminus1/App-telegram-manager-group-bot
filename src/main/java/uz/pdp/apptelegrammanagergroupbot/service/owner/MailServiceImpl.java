package uz.pdp.apptelegrammanagergroupbot.service.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import uz.pdp.apptelegrammanagergroupbot.entity.CreatorCode;
import uz.pdp.apptelegrammanagergroupbot.repository.CreatorCodeRepository;
import uz.pdp.apptelegrammanagergroupbot.utils.AppConstant;

@RequiredArgsConstructor
@Service
@EnableAsync
public class MailServiceImpl implements MailService {
    private final MailSender mailSender;
    private final CodeService codeService;
    private final CreatorCodeRepository creatorCodeRepository;

    @Async
    @Override
    public void sendCodeForCreator() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(AppConstant.EMAIL_FOR_GET_CREATOR_PASS);
        message.setSubject("Creator");
        String codeString = codeService.generateCode();
        creatorCodeRepository.save(new CreatorCode(codeString));
        message.setText(AppConstant.GET_CREATOR_TEXT + codeString);
        mailSender.send(message);
    }


}
