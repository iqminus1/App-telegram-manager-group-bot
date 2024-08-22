package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "groups")
public class Group extends AbsLongEntity implements Serializable {
    private Long ownerId;

    private Long groupId;

    private String cardNumber;

    private boolean payment;

    private boolean code;

    private boolean screenShot;

    @OneToMany(mappedBy = "group",fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Tariff> tariffs;
}
