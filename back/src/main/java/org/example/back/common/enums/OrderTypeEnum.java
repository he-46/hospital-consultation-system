package org.example.back.common.enums;

import lombok.Getter;

@Getter
public enum OrderTypeEnum {
    REGISTER(1, "挂号订单"),
    CONSULT(2, "咨询订单");

    private final Integer code;
    private final String desc;

    OrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}