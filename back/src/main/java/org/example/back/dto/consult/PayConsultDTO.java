package org.example.back.dto.consult;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class PayConsultDTO {
    @NotNull(message = "支付方式不能为空 1支付宝 2微信")
    private Integer payType;
}