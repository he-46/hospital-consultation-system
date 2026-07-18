package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.entity.HospitalDepartment;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalDepartmentMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private HospitalDepartmentMapper hospitalDepartmentMapper;

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, Long departmentId, String level) {
        Page<Hospital> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1)
               .orderByDesc(Hospital::getFollowCount);

        // 二级科室筛选
        if (departmentId != null && departmentId > 0) {
            wrapper.in(Hospital::getId, getHospitalIdsByDepartment(departmentId));
        }

        // 等级筛选
        if (StringUtils.hasText(level)) {
            wrapper.eq(Hospital::getLevel, level);
        }

        Page<Hospital> result = this.page(page, wrapper);

        // 填充科室标签
        fillDepartmentTags(result.getRecords());

        return result;
    }

    @Override
    public List<Hospital> getHotHospitals(Integer limit) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1)
               .orderByDesc(Hospital::getFollowCount)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getHospitalDetail(Long id) {
        Hospital hospital = this.getById(id);
        if (hospital == null) {
            throw new RuntimeException("医院不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hospital", hospital);

        // 获取该医院的科室列表（通过关联表）
        List<Department> departments = getDepartmentsByHospital(id);
        result.put("departments", departments);

        // 获取该医院的医生列表（取前10个）
        LambdaQueryWrapper<Doctor> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(Doctor::getHospitalId, id)
                  .eq(Doctor::getStatus, 1)
                  .orderByDesc(Doctor::getRating)
                  .last("LIMIT 10");
        List<Doctor> doctors = doctorMapper.selectList(docWrapper);

        // 填充医生科室名
        if (!doctors.isEmpty()) {
            Set<Long> deptIds = doctors.stream()
                    .map(Doctor::getDepartmentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, String> deptNameMap = departmentMapper.selectBatchIds(deptIds).stream()
                    .collect(Collectors.toMap(Department::getId, Department::getName));
            for (Doctor doctor : doctors) {
                doctor.setDepartmentName(deptNameMap.get(doctor.getDepartmentId()));
            }
        }
        result.put("doctors", doctors);

        return result;
    }

    @Override
    public Page<Hospital> searchHospitals(String keyword, Integer pageNum, Integer pageSize) {
        Page<Hospital> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Hospital::getName, keyword)
                    .or().like(Hospital::getAddress, keyword));
        }

        wrapper.orderByDesc(Hospital::getFollowCount);
        return this.page(page, wrapper);
    }

    @Override
    public Page<Doctor> getDoctorsByHospital(Long hospitalId, Integer pageNum, Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getHospitalId, hospitalId)
                .eq(Doctor::getStatus, 1)
                .orderByDesc(Doctor::getRating);
        Page<Doctor> result = doctorMapper.selectPage(page, wrapper);

        // 填充医院和科室名称
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Set<Long> deptIds = result.getRecords().stream()
                    .map(Doctor::getDepartmentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, String> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                    .collect(Collectors.toMap(Department::getId, Department::getName));
            for (Doctor doctor : result.getRecords()) {
                doctor.setDepartmentName(deptMap.get(doctor.getDepartmentId()));
            }
        }

        return result;
    }

    @Override
    public List<Department> getDepartmentsByHospital(Long hospitalId) {
        // 通过关联表查询该医院关联的所有科室ID
        LambdaQueryWrapper<HospitalDepartment> linkWrapper = new LambdaQueryWrapper<>();
        linkWrapper.eq(HospitalDepartment::getHospitalId, hospitalId);
        List<HospitalDepartment> links = hospitalDepartmentMapper.selectList(linkWrapper);

        if (links.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> deptIds = links.stream()
                .map(HospitalDepartment::getDepartmentId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.in(Department::getId, deptIds)
                .eq(Department::getStatus, 1)
                .orderByAsc(Department::getSortOrder);

        return departmentMapper.selectList(deptWrapper);
    }

    /**
     * 根据科室ID获取关联的医院ID列表
     */
    private Set<Long> getHospitalIdsByDepartment(Long departmentId) {
        LambdaQueryWrapper<HospitalDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HospitalDepartment::getDepartmentId, departmentId);
        List<HospitalDepartment> links = hospitalDepartmentMapper.selectList(wrapper);
        return links.stream()
                .map(HospitalDepartment::getHospitalId)
                .collect(Collectors.toSet());
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
