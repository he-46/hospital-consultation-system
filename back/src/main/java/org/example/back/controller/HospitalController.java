package org.example.back.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.back.common.Result;
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
     * 获取医院列表
     */
    @GetMapping("/list")
    public Result<?> getHospitalList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long departmentId) {
        Page<Hospital> page = hospitalService.getHospitalPage(pageNum, pageSize, departmentId);
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
     * 获取医院详情
     */
    @GetMapping("/detail")
    public Result<?> getHospitalDetail(@RequestParam Long id) {
        Map<String, Object> detail = hospitalService.getHospitalDetail(id);
        return Result.success(detail);
    }

    /**
     * 搜索医院
     */
    @GetMapping("/search")
    public Result<?> searchHospitals(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Hospital> page = hospitalService.searchHospitals(keyword, pageNum, pageSize);
        return Result.success(page);
    }
}
