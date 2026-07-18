package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_appointment")
public class Appointment implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long doctorId;

    private Long hospitalId;

    private Long scheduleId;

    private String patientName;

    private String patientPhone;

    private String patientIdCard;

    private Integer patientGender;

    private Integer patientAge;

    private LocalDate appointmentDate;

    private String appointmentTime;

    private String diseaseDesc;

    private BigDecimal amount;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
