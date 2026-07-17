package org.example.back.service;

import org.example.back.entity.Department;

import java.util.List;

public interface DepartmentService {
    
    /**
     * 获取一级科室列表
     */
    List<Department> getPrimaryDepartments();
    
    /**
     * 获取二级科室列表（根据父级ID）
     */
    List<Department> getSecondaryDepartments(Long parentId);
    
    /**
     * 获取所有科室（树形结构）
     */
    List<Department> getDepartmentTree();
}
