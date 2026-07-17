package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室控制器
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取一级科室列表
     */
    @GetMapping("/primary")
    public Result<?> getPrimaryDepartments() {
        List<Department> departments = departmentService.getPrimaryDepartments();
        return Result.success(departments);
    }

    /**
     * 获取二级科室列表
     */
    @GetMapping("/secondary")
    public Result<?> getSecondaryDepartments(@RequestParam Long parentId) {
        List<Department> departments = departmentService.getSecondaryDepartments(parentId);
        return Result.success(departments);
    }

    /**
     * 获取科室树形结构
     */
    @GetMapping("/tree")
    public Result<?> getDepartmentTree() {
        List<Department> departments = departmentService.getDepartmentTree();
        return Result.success(departments);
    }

    /**
     * 某科室下的医生列表 - GET /api/department/{id}/doctors
     */
    @GetMapping("/{id}/doctors")
    public Result<?> getDoctorsByDepartment(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = departmentService.getDoctorsByDepartment(id, pageNum, pageSize);
        return Result.success(page);
    }
}
