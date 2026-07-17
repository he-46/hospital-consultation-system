package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;

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

    /**
     * 根据科室ID查医生列表（分页）
     */
    Page<Doctor> getDoctorsByDepartment(Long departmentId, Integer pageNum, Integer pageSize);
}
