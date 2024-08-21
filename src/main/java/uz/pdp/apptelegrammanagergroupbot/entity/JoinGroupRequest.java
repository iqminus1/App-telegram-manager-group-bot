package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrammanagergroupbot.enums.JoinTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class JoinGroupRequest extends AbsLongEntity {
    private Long userId;

    private Long groupId;

    private JoinTypeEnum type;
}
