package uz.pdp.apptelegrammanagergroupbot.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class Tariff extends AbsLongEntity implements Serializable, Comparable<Tariff> {
    @ManyToOne
    private Group group;

    private String name;

    private Integer days;

    private Long price;

    private Integer orderBy;

    @Override
    public int compareTo(Tariff other) {
        return this.orderBy.compareTo(other.orderBy);
    }
}
