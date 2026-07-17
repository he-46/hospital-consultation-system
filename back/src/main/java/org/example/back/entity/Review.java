package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_review")
public class Review implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer orderType;

    private Long orderId;

    private Long userId;

    private Long doctorId;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;
}
