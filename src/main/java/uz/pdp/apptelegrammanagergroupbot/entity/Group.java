package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "groups")
public class Group extends AbsLongEntity {
    private Long ownerId;

    private Long groupId;

    private String cardNumber;

    private boolean payment;

    private boolean code;

    private boolean screenShot;

    @OneToMany(mappedBy = "groupId")
    @ToString.Exclude
    private List<Tariff> tariffs;
}
