package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Tariff extends AbsLongEntity {
    @ManyToOne
    private Group groupId;

    private String name;

    private Integer days;

    private Long price;

    private Integer orderBy;
}
