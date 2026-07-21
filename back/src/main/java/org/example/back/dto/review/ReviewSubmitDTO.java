package org.example.back.dto.review;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ReviewSubmitDTO {
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotNull(message = "医生id不能为空")
    private Long doctorId;
    @Min(1)
    @Max(5)
    @NotNull(message = "评分1-5")
    private Integer score;
    private String content;
}