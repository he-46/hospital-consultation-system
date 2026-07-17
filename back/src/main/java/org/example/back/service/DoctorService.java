package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Doctor;

import java.util.List;
import java.util.Map;

public interface DoctorService {
    
    /**
     * 获取医生列表（分页）
     */
    Page<Doctor> getDoctorPage(Integer pageNum, Integer pageSize, Long departmentId, Long hospitalId);
    
    /**
     * 获取热门医生
     */
    List<Doctor> getHotDoctors(Integer limit);
    
    /**
     * 获取医生详情
     */
    Map<String, Object> getDoctorDetail(Long id);
    
    /**
     * 搜索医生
     */
    Page<Doctor> searchDoctors(String keyword, Long departmentId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取医生的所有预约时间
     */
    List<Map<String, Object>> getDoctorSchedule(Long doctorId);
}
