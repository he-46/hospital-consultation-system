package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_consult")
public class Consult {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long doctorId;
    private String patientName;
    private String patientPhone;
    private String diseaseDesc;
    private LocalDateTime appointmentTime;
    private Integer duration;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展字段（非数据库字段）
    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String doctorTitle;
    @TableField(exist = false)
    private String hospitalName;
    @TableField(exist = false)
    private Boolean hasReview;
}