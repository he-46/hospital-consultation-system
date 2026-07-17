package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    
    private Long departmentId;
    
    private String name;
    
    private String description;
    
    private String alias;
    
    private String location;
    
    private String treatment;
    
    private String symptoms;
    
    private String treatmentPeriod;
    
    private String cureRate;
    
    private String examinations;
    
    private Integer followCount;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 扩展字段
    private String departmentName;
}
