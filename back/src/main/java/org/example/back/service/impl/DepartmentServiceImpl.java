package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Department;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> getPrimaryDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1)
               .eq(Department::getParentId, 0)
               .orderByAsc(Department::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public List<Department> getSecondaryDepartments(Long parentId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1)
               .eq(Department::getParentId, parentId)
               .orderByAsc(Department::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public List<Department> getDepartmentTree() {
        // 获取所有一级科室
        List<Department> primaryList = getPrimaryDepartments();
        
        for (Department primary : primaryList) {
            List<Department> secondaryList = getSecondaryDepartments(primary.getId());
            primary.setChildren(secondaryList);
        }
        
        return primaryList;
    }
}
