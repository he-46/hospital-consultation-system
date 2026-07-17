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
import org.example.back.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, Long departmentId) {
        Page<Hospital> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1)
               .orderByDesc(Hospital::getFollowCount);
        
        if (departmentId != null && departmentId > 0) {
            // 根据科室查询关联的医院
            // 先获取该科室下的所有医院ID
            wrapper.apply("SELECT id FROM t_hospital WHERE id IN " +
                "(SELECT hospital_id FROM t_hospital_department WHERE department_id = " + departmentId + ")");
        }
        
        return this.page(page, wrapper);
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
        
        // 获取该医院的科室列表
        LambdaQueryWrapper<Department> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(Department::getStatus, 1)
                   .eq(Department::getParentId, 0)
                   .orderByAsc(Department::getSortOrder);
        List<Department> departments = departmentMapper.selectList(deptWrapper);
        result.put("departments", departments);
        
        // 获取该医院的医生列表
        LambdaQueryWrapper<Doctor> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(Doctor::getHospitalId, id)
                  .eq(Doctor::getStatus, 1)
                  .orderByDesc(Doctor::getRating)
                  .last("LIMIT 10");
        List<Doctor> doctors = doctorMapper.selectList(docWrapper);
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
            java.util.Set<Long> deptIds = result.getRecords().stream()
                    .map(Doctor::getDepartmentId)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Map<Long, String> deptMap = departmentMapper.selectBatchIds(deptIds).stream()
                    .collect(java.util.stream.Collectors.toMap(Department::getId, Department::getName));
            for (Doctor doctor : result.getRecords()) {
                doctor.setDepartmentName(deptMap.get(doctor.getDepartmentId()));
            }
        }

        return result;
    }
}
