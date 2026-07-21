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
@TableName("t_hospital")
public class Hospital implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String level;

    private String address;
    
    private String phone;
    
    private String intro;
    
    private String image;
    
    private String province;
    
    private String city;
    
    private Integer departmentCount;
    
    private Integer doctorCount;
    
    private Integer followCount;
    
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 非数据库字段：科室标签列表
    @TableField(exist = false)
    private List<String> departmentTags;
}
