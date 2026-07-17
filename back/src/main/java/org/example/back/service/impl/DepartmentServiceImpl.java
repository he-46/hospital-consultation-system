package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

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

    @Override
    public Page<Doctor> getDoctorsByDepartment(Long departmentId, Integer pageNum, Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDepartmentId, departmentId)
                .eq(Doctor::getStatus, 1)
                .orderByDesc(Doctor::getRating);
        Page<Doctor> result = doctorMapper.selectPage(page, wrapper);

        // 填充医院和科室名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Set<Long> hospitalIds = result.getRecords().stream()
                    .map(Doctor::getHospitalId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, String> hospitalMap = hospitalMapper.selectBatchIds(hospitalIds).stream()
                    .collect(Collectors.toMap(Hospital::getId, Hospital::getName));
            for (Doctor doctor : result.getRecords()) {
                doctor.setHospitalName(hospitalMap.get(doctor.getHospitalId()));
            }
        }

        return result;
    }
}
