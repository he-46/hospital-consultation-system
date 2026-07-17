package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("t_department")
public class Department implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private Long parentId;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    // 非数据库字段，用于树形结构
    @TableField(exist = false)
    private List<Department> children;
}
