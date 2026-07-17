package org.example.back.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;

import java.util.List;
import java.util.Map;

public interface HospitalService {

    /**
     * 获取医院列表（分页）
     */
    Page<Hospital> getHospitalPage(Integer pageNum, Integer pageSize, Long departmentId);

    /**
     * 获取热门医院
     */
    List<Hospital> getHotHospitals(Integer limit);

    /**
     * 获取医院详情
     */
    Map<String, Object> getHospitalDetail(Long id);

    /**
     * 搜索医院
     */
    Page<Hospital> searchHospitals(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据医院ID查医生列表（分页）
     */
    Page<Doctor> getDoctorsByHospital(Long hospitalId, Integer pageNum, Integer pageSize);
}
