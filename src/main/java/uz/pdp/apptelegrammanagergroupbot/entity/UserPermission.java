package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class UserPermission extends AbsLongEntity {
    private Long userId;

    private String name;

    private String botToken;

    private String botUsername;

    private String contactNumber;

    private Timestamp expireDate;

    private Integer sizeRequests;

    private boolean payment;

    private boolean code;

    private boolean screenshot;
}
