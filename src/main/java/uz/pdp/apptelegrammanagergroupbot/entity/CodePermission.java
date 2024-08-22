package uz.pdp.apptelegrammanagergroupbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import uz.pdp.apptelegrammanagergroupbot.entity.temp.AbsLongEntity;
import uz.pdp.apptelegrammanagergroupbot.enums.CodeType;

import java.io.Serializable;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class CodePermission extends AbsLongEntity implements Serializable {
    private Long createBy;

    private Long usedFrom;

    private String code;

    private String path;

    private Timestamp createdDate;

    private Integer expireMonth;

    private Integer sizeRequests;

    @Enumerated(EnumType.STRING)
    private CodeType type;

    private boolean payment;

    private boolean screenshot;

    private boolean codeGeneration;

}
