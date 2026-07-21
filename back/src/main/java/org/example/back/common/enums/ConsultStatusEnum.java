package org.example.back.common.enums;

import lombok.Getter;

@Getter
public enum ConsultStatusEnum {
    PENDING_PAY(1, "待支付"),
    PAID(2, "已支付"),
    CONSULTING(3, "咨询中"),
    FINISHED(4, "已完成"),
    CANCEL(5, "已取消");

    private final Integer code;
    private final String desc;

    ConsultStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}