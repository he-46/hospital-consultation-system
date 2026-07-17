package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.entity.Schedule;
import org.example.back.mapper.DepartmentMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.mapper.ScheduleMapper;
import org.example.back.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Autowired
    private DepartmentMapper departmentMapper;
    
    @Autowired
    private HospitalMapper hospitalMapper;
    
    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public Page<Doctor> getDoctorPage(Integer pageNum, Integer pageSize, Long departmentId, Long hospitalId) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1);
        
        if (departmentId != null && departmentId > 0) {
            wrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        if (hospitalId != null && hospitalId > 0) {
            wrapper.eq(Doctor::getHospitalId, hospitalId);
        }
        
        wrapper.orderByDesc(Doctor::getRating);
        Page<Doctor> result = this.page(page, wrapper);
        
        // 填充医院和科室名称
        fillHospitalAndDepartmentName(result.getRecords());
        
        return result;
    }

    @Override
    public List<Doctor> getHotDoctors(Integer limit) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1)
               .orderByDesc(Doctor::getFollowCount)
               .last("LIMIT " + limit);
        List<Doctor> doctors = this.list(wrapper);
        
        fillHospitalAndDepartmentName(doctors);
        return doctors;
    }

    @Override
    public Map<String, Object> getDoctorDetail(Long id) {
        Doctor doctor = this.getById(id);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取医院信息
        Hospital hospital = hospitalMapper.selectById(doctor.getHospitalId());
        if (hospital != null) {
            doctor.setHospitalName(hospital.getName());
        }
        
        // 获取科室信息
        Department department = departmentMapper.selectById(doctor.getDepartmentId());
        if (department != null) {
            doctor.setDepartmentName(department.getName());
        }
        
        result.put("doctor", doctor);
        
        // 获取排班信息
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getDoctorId, id)
                       .ge(Schedule::getScheduleDate, new Date())
                       .eq(Schedule::getStatus, 1)
                       .orderByAsc(Schedule::getScheduleDate);
        List<Schedule> schedules = scheduleMapper.selectList(scheduleWrapper);
        result.put("schedules", schedules);
        
        return result;
    }

    @Override
    public Page<Doctor> searchDoctors(String keyword, Long departmentId, Integer pageNum, Integer pageSize) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Doctor::getName, keyword)
                    .or().like(Doctor::getExpertise, keyword));
        }
        if (departmentId != null && departmentId > 0) {
            wrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        
        wrapper.orderByDesc(Doctor::getRating);
        Page<Doctor> result = this.page(page, wrapper);
        
        fillHospitalAndDepartmentName(result.getRecords());
        return result;
    }

    @Override
    public List<Map<String, Object>> getDoctorSchedule(Long doctorId) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .ge(Schedule::getScheduleDate, new Date())
               .eq(Schedule::getStatus, 1)
               .orderByAsc(Schedule::getScheduleDate);
        List<Schedule> schedules = scheduleMapper.selectList(wrapper);
        
        return schedules.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("scheduleDate", s.getScheduleDate());
            map.put("timeSlot", s.getTimeSlot());
            map.put("totalCount", s.getTotalCount());
            map.put("remainCount", s.getRemainCount());
            return map;
        }).collect(Collectors.toList());
    }
    
    private void fillHospitalAndDepartmentName(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }
        
        // 获取所有医院ID
        Set<Long> hospitalIds = doctors.stream()
                .map(Doctor::getHospitalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 获取所有科室ID
        Set<Long> departmentIds = doctors.stream()
                .map(Doctor::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 批量查询
        Map<Long, String> hospitalMap = hospitalMapper.selectBatchIds(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getId, Hospital::getName));
        
        Map<Long, String> departmentMap = departmentMapper.selectBatchIds(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
        
        // 填充名称
        for (Doctor doctor : doctors) {
            doctor.setHospitalName(hospitalMap.get(doctor.getHospitalId()));
            doctor.setDepartmentName(departmentMap.get(doctor.getDepartmentId()));
        }
    }
}
