package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
import org.example.back.entity.Doctor;
import org.example.back.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医生控制器
 */
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * 获取医生列表
     */
    @GetMapping("/list")
    public Result<?> getDoctorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long hospitalId) {
        Page<Doctor> page = doctorService.getDoctorPage(pageNum, pageSize, departmentId, hospitalId);
        return Result.success(page);
    }

    /**
     * 获取热门医生
     */
    @GetMapping("/hot")
    public Result<?> getHotDoctors(@RequestParam(defaultValue = "10") Integer limit) {
        List<Doctor> doctors = doctorService.getHotDoctors(limit);
        return Result.success(doctors);
    }

    /**
     * 获取医生详情
     */
    @GetMapping("/detail")
    public Result<?> getDoctorDetail(@RequestParam Long id) {
        Map<String, Object> detail = doctorService.getDoctorDetail(id);
        return Result.success(detail);
    }

    /**
     * 搜索医生
     */
    @GetMapping("/search")
    public Result<?> searchDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Doctor> page = doctorService.searchDoctors(keyword, departmentId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取医生排班
     */
    @GetMapping("/schedule")
    public Result<?> getDoctorSchedule(@RequestParam Long doctorId) {
        List<Map<String, Object>> schedules = doctorService.getDoctorSchedule(doctorId);
        return Result.success(schedules);
    }
}
