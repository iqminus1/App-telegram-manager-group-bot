package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Invoice extends AbsLongEntity implements Serializable {
    private String number;

    private Long userId;

    private Long groupId;

    private Long amount;

    private Integer expire;
}
