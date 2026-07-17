package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_doctor")
public class Doctor implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer gender;
    
    private String title;
    
    private Long departmentId;
    
    private Long hospitalId;
    
    private String avatar;
    
    private String phone;
    
    private String intro;
    
    private String expertise;
    
    private Integer consultCount;
    
    private BigDecimal rating;
    
    private Integer followCount;
    
    private Integer onlineStatus;
    
    private BigDecimal price;
    
    private BigDecimal registrationPrice;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 扩展字段
    @TableField(exist = false)
    private String hospitalName;
    
    @TableField(exist = false)
    private String departmentName;
}
