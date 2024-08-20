package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class JoinGroupRequest extends AbsLongEntity {
    private Long chatId;

    private Long groupId;
}
