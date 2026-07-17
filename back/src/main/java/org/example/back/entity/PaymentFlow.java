package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_payment_flow")
public class PaymentFlow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String businessOrderNo;

    private Integer businessType;

    private Integer payMethod;

    private String thirdPartyTradeNo;

    private BigDecimal actualAmount;

    private Integer payStatus;

    private LocalDateTime paySuccessTime;

    private String originalCallback;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
