package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrammanagergroupbot.enums.ScreenshotStatus;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@SQLRestriction("active = false")
public class ScreenshotGroup extends AbsLongEntity {
    private Long groupId;

    private Long sendUserId;

    private String path;

    private ScreenshotStatus status;

    private boolean active;

    private Timestamp activeDate;
}
