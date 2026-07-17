package org.example.back.common.enums;

import lombok.Getter;

@Getter
public enum FollowTypeEnum {
    HOSPITAL(1, "医院"),
    DOCTOR(2, "医生"),
    DISEASE(3, "疾病");

    private final Integer code;
    private final String desc;

    FollowTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}