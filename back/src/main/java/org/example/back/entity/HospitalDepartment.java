package org.example.back.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_hospital_department")
public class HospitalDepartment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hospitalId;

    private Long departmentId;

    private LocalDateTime createTime;
}
