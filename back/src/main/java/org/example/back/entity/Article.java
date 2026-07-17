package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class Article implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String summary;
    
    private String content;
    
    @TableField("department_id")
    private Long departmentId;
    
    private String author;
    
    private String image;
    
    private Integer views;
    
    private Integer status;
    
    @TableField("publish_time")
    private LocalDateTime publishTime;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
