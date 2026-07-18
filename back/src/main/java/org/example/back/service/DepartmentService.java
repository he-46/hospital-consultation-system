package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;

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
     * 获取所有科室（树形结构，带Redis缓存）
     */
    List<Department> getDepartmentTree();

    /**
     * 根据科室ID查医生列表（分页）
     */
    Page<Doctor> getDoctorsByDepartment(Long departmentId, Integer pageNum, Integer pageSize);

    /**
     * 根据二级科室ID查关联的医院列表（分页）
     */
    Page<Hospital> getHospitalsByDepartment(Long departmentId, Integer pageNum, Integer pageSize);
}
