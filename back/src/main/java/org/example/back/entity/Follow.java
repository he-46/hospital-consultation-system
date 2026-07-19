package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_follow")
public class Follow {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer followType;
    private Long followId;
    private LocalDateTime createTime;
}