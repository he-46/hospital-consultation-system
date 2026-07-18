package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.RedisUtil;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.entity.HospitalDepartment;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalDepartmentMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private HospitalDepartmentMapper hospitalDepartmentMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String DEPT_TREE_CACHE_KEY = "department:tree";

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
    @SuppressWarnings("unchecked")
    public List<Department> getDepartmentTree() {
        // 先从 Redis 缓存中获取
        Object cached = redisUtil.get(DEPT_TREE_CACHE_KEY);
        if (cached instanceof List) {
            return (List<Department>) cached;
        }

        // 缓存未命中，从数据库查询
        List<Department> primaryList = getPrimaryDepartments();

        for (Department primary : primaryList) {
            List<Department> secondaryList = getSecondaryDepartments(primary.getId());
            primary.setChildren(secondaryList);
        }

        // 写入 Redis 缓存，过期时间 30 分钟
        redisUtil.set(DEPT_TREE_CACHE_KEY, primaryList, 30, TimeUnit.MINUTES);

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

    @Override
    public Page<Hospital> getHospitalsByDepartment(Long departmentId, Integer pageNum, Integer pageSize) {
        Page<Hospital> page = new Page<>(pageNum, pageSize);

        // 通过关联表查询该科室下的所有医院
        LambdaQueryWrapper<HospitalDepartment> linkWrapper = new LambdaQueryWrapper<>();
        linkWrapper.eq(HospitalDepartment::getDepartmentId, departmentId);
        List<HospitalDepartment> links = hospitalDepartmentMapper.selectList(linkWrapper);

        if (links.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        Set<Long> hospitalIds = links.stream()
                .map(HospitalDepartment::getHospitalId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<Hospital> hospitalWrapper = new LambdaQueryWrapper<>();
        hospitalWrapper.in(Hospital::getId, hospitalIds)
                .eq(Hospital::getStatus, 1)
                .orderByDesc(Hospital::getFollowCount);

        Page<Hospital> result = hospitalMapper.selectPage(page, hospitalWrapper);

        // 填充科室标签
        fillDepartmentTags(result.getRecords());

        return result;
    }

    /**
     * 填充医院列表的科室标签
     */
    private void fillDepartmentTags(List<Hospital> hospitals) {
        if (hospitals == null || hospitals.isEmpty()) return;

        Set<Long> hospitalIds = hospitals.stream().map(Hospital::getId).collect(Collectors.toSet());

        List<HospitalDepartment> links = hospitalDepartmentMapper.selectList(
                new LambdaQueryWrapper<HospitalDepartment>()
                        .in(HospitalDepartment::getHospitalId, hospitalIds)
        );

        if (links.isEmpty()) return;

        Set<Long> deptIds = links.stream()
                .map(HospitalDepartment::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> deptNameMap = departmentMapper.selectBatchIds(deptIds).stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .collect(Collectors.toMap(Department::getId, Department::getName));

        Map<Long, List<String>> tagMap = links.stream()
                .collect(Collectors.groupingBy(
                        HospitalDepartment::getHospitalId,
                        Collectors.mapping(l -> deptNameMap.get(l.getDepartmentId()),
                                Collectors.filtering(Objects::nonNull, Collectors.toList()))
                ));

        for (Hospital hospital : hospitals) {
            hospital.setDepartmentTags(tagMap.getOrDefault(hospital.getId(), Collections.emptyList()));
        }
    }
}
