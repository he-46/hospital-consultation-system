package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.back.entity.Department;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
