package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_feedback")
public class Feedback implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("feedback_type")
    private Integer feedbackType;    // 1=系统问题 2=服务问题 3=医生问题 4=其他

    private String content;

    private String images;           // 图片URL, 逗号分隔

    private Integer status;          // 1=待处理 2=已处理

    @TableField("reply_content")
    private String replyContent;     // 管理员回复

    @TableField("reply_time")
    private LocalDateTime replyTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
