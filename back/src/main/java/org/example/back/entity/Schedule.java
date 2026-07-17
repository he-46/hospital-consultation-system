package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule")
public class Schedule implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long doctorId;
    
    private Long hospitalId;
    
    private Long departmentId;
    
    private LocalDate scheduleDate;
    
    private String timeSlot;
    
    private Integer totalCount;
    
    private Integer remainCount;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
