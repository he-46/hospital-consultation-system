package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_disease")
public class Disease implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("department_id")
    private Long departmentId;
    
    private String name;
    
    private String description;
    
    private String alias;
    
    private String location;
    
    private String treatment;
    
    private String symptoms;
    
    @TableField("treatment_period")
    private String treatmentPeriod;
    
    @TableField("cure_rate")
    private String cureRate;
    
    private String examinations;
    
    @TableField("follow_count")
    private Integer followCount;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 扩展字段（非数据库字段）
    @TableField(exist = false)
    private String departmentName;
}
