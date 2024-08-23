package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;

import java.io.Serializable;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class ScreenshotGroup extends AbsLongEntity implements Serializable {
    private Long groupId;

    private Long sendUserId;

    private Long tariffId;

    private String path;

    @Enumerated(EnumType.STRING)
    private ScreenshotStatus status;

    private boolean active;

    private Timestamp activeDate;
}
