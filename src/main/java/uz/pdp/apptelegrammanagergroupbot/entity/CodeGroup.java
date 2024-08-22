package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.io.Serializable;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@SQLRestriction("active = false")
public class CodeGroup extends AbsLongEntity implements Serializable {
    private String code;

    private Long groupId;

    private Long userId;

    private Integer expireDay;

    private boolean active;

    private Timestamp activeAt;
}
