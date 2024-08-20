package uz.pdp.apptelegrammanagergroupbot.entity.temp;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class AbsLongEntity {
    @Id
    @GeneratedValue
    private Long id;
}
