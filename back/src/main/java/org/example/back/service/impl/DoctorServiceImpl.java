package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.RedisUtil;
import org.example.back.entity.*;
import org.example.back.mapper.*;
import org.example.back.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String HOT_DOCTORS_KEY = "hot_doctors:";
    private static final long HOT_DOCTORS_TTL = 30; // 30分钟

    @Override
    public Page<Doctor> getDoctorPage(Integer pageNum, Integer pageSize, Long departmentId, Long hospitalId, String title) {
        Page<Doctor> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1);

        if (departmentId != null && departmentId > 0) {
            // 查找子科室，有子科室则一并查询
            List<Department> children = departmentMapper.selectList(
                    new LambdaQueryWrapper<Department>().eq(Department::getParentId, departmentId));
            if (children.isEmpty()) {
                wrapper.eq(Doctor::getDepartmentId, departmentId);
            } else {
                List<Long> deptIds = new java.util.ArrayList<>();
                deptIds.add(departmentId);
                children.forEach(c -> deptIds.add(c.getId()));
                wrapper.in(Doctor::getDepartmentId, deptIds);
            }
        }
        if (hospitalId != null && hospitalId > 0) {
            wrapper.eq(Doctor::getHospitalId, hospitalId);
        }
        if (StringUtils.hasText(title)) {
            wrapper.eq(Doctor::getTitle, title);
        }

        wrapper.orderByDesc(Doctor::getRating);
        Page<Doctor> result = this.page(page, wrapper);

        fillHospitalAndDepartmentName(result.getRecords());

        return result;
    }

    @Override
    public List<Doctor> getHotDoctors(Integer limit) {
        String cacheKey = HOT_DOCTORS_KEY + limit;

        // 尝试从Redis获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null && cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<Doctor> cachedList = (List<Doctor>) cached;
            return cachedList;
        }

        // 查数据库
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getStatus, 1)
                .eq(Doctor::getOnlineStatus, 1)
                .orderByDesc(Doctor::getFollowCount)
                .orderByDesc(Doctor::getRating)
                .last("LIMIT " + limit);
        List<Doctor> doctors = this.list(wrapper);

        fillHospitalAndDepartmentName(doctors);

        // 写入Redis，30分钟过期
        redisUtil.set(cacheKey, doctors, HOT_DOCTORS_TTL, TimeUnit.MINUTES);

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
    public Page<Map<String, Object>> getDoctorReviews(Long doctorId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getDoctorId, doctorId)
                .orderByDesc(Review::getCreateTime);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        // 获取所有用户ID，批量查询用户名
        Set<Long> userIds = reviewPage.getRecords().stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u.getRealName()));
        }

        // 构造返回数据
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, reviewPage.getTotal());
        List<Map<String, Object>> records = reviewPage.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("createTime", r.getCreateTime());
            map.put("userName", userMap.getOrDefault(r.getUserId(), "匿名用户"));
            return map;
        }).collect(Collectors.toList());
        resultPage.setRecords(records);

        return resultPage;
    }

    private void fillHospitalAndDepartmentName(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }

        Set<Long> hospitalIds = doctors.stream()
                .map(Doctor::getHospitalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> departmentIds = doctors.stream()
                .map(Doctor::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> hospitalMap = hospitalMapper.selectBatchIds(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getId, Hospital::getName));

        Map<Long, String> departmentMap = departmentMapper.selectBatchIds(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        for (Doctor doctor : doctors) {
            doctor.setHospitalName(hospitalMap.get(doctor.getHospitalId()));
            doctor.setDepartmentName(departmentMap.get(doctor.getDepartmentId()));
        }
    }
}
