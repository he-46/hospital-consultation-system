package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Doctor;

import java.util.List;
import java.util.Map;

public interface DoctorService {

    /**
     * 获取医生分页列表（支持按科室、医院、职称筛选）
     */
    Page<Doctor> getDoctorPage(Integer pageNum, Integer pageSize, Long departmentId, Long hospitalId, String title);

    /**
     * 获取热门医生（Redis缓存30分钟）
     */
    List<Doctor> getHotDoctors(Integer limit);

    /**
     * 获取医生详情
     */
    Map<String, Object> getDoctorDetail(Long id);

    /**
     * 搜索医生（按姓名或擅长领域）
     */
    Page<Doctor> searchDoctors(String keyword, Long departmentId, Integer pageNum, Integer pageSize);

    /**
     * 获取医生评价列表（分页）
     */
    Page<Map<String, Object>> getDoctorReviews(Long doctorId, Integer pageNum, Integer pageSize);
}
