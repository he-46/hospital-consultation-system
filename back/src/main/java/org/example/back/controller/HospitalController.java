package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Department;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医院控制器
 */
@RestController
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     * 获取医院列表（分页）- 原接口
     */
    @GetMapping("/list")
    public Result<?> getHospitalList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String level) {
        Page<Hospital> page = hospitalService.getHospitalPage(pageNum, pageSize, departmentId, level);
        return Result.success(page);
    }

    /**
     * 2-3 医院分页列表 - GET /api/hospitals
     * 支持按二级科室筛选、按等级筛选、分页，返回名称/等级/地址/图片/科室标签/关注数
     */
    @GetMapping("/hospitals")
    public Result<?> getHospitals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String level) {
        Page<Hospital> page = hospitalService.getHospitalPage(pageNum, pageSize, departmentId, level);
        return Result.success(page);
    }

    /**
     * 获取热门医院
     */
    @GetMapping("/hot")
    public Result<?> getHotHospitals(@RequestParam(defaultValue = "8") Integer limit) {
        List<Hospital> hospitals = hospitalService.getHotHospitals(limit);
        return Result.success(hospitals);
    }

    /**
     * 获取医院详情 - 原接口
     */
    @GetMapping("/detail")
    public Result<?> getHospitalDetail(@RequestParam Long id) {
        Map<String, Object> detail = hospitalService.getHospitalDetail(id);
        return Result.success(detail);
    }

    /**
     * 2-5 医院详情 - GET /api/hospitals/{id}
     * 返回完整信息 + 该医院科室列表 + 医生数量 + 关注数
     */
    @GetMapping("/hospitals/{id}")
    public Result<?> getHospitalById(@PathVariable Long id) {
        Map<String, Object> detail = hospitalService.getHospitalDetail(id);
        return Result.success(detail);
    }

    /**
     * 2-4 搜索医院 - GET /api/hospitals/search
     */
    @GetMapping("/hospitals/search")
    public Result<?> searchHospitals(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Hospital> page = hospitalService.searchHospitals(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 搜索医院 - 原接口
     */
    @GetMapping("/search")
    public Result<?> searchHospitalsOld(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Hospital> page = hospitalService.searchHospitals(keyword, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 某医院下的医生列表 - GET /api/hospital/{id}/doctors
     */
    @GetMapping("/{id}/doctors")
    public Result<?> getDoctorsByHospital(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = hospitalService.getDoctorsByHospital(id, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 2-6 某医院下的科室列表 - GET /api/hospitals/{id}/departments
     */
    @GetMapping("/hospitals/{id}/departments")
    public Result<?> getDepartmentsByHospital(@PathVariable Long id) {
        List<Department> departments = hospitalService.getDepartmentsByHospital(id);
        return Result.success(departments);
    }
}
