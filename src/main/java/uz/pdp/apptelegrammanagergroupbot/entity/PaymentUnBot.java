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
public class PaymentUnBot extends AbsLongEntity {
    private Long userId;

    private Long groupId;

    private Integer days;

    private String photoPath;
}
